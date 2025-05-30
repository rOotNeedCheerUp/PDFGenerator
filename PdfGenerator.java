/**
 * 上传一个指定背景的PDF模板，生成一个PDF文件。
 * 只适应于A4纸大小的PDF模板。
 */
public class PdfGenerator {

    private final PDFont wordsFont; // 字体对象
    private final PDRectangle mediaBox; // 页面矩形对象
    private final PDDocument document;// 模板PDF文档

    // 内部类定义文字位置参数
    public static class TextPosition {
        private final String content;
        private final float x;
        private final float y;
        private final PDFont font;
        private final float fontSize;
        private final boolean isBold;

        public TextPosition(String content, float x, float y, PDFont font, float fontSize, boolean isBold) {
            this.content = content;
            this.x = x;
            this.y = y;
            this.font = font;
            this.fontSize = fontSize;
            this.isBold = isBold;
        }
    }

    /**
     * 构造函数，加载字体和模板PDF
     * @param wordsFontPath 字体文件路径
     * @param inputPath 模板PDF路径
     */
    public PdfGenerator(String wordsFontPath, String inputPath) {
        // 获取字体
        try {
            File file = new File(wordsFontPath);
            this.wordsFont = PDType0Font.load(new PDDocument(), file);
        } catch (IOException e) {
            throw new RuntimeException("加载字体文件失败: " + e.getMessage(), e);
        }
        // 加载模板PDF
        try {
            this.document = Loader.loadPDF(new File(inputPath));
            PDPage page = document.getPage(0);
            this.mediaBox = page.getMediaBox();
        } catch (IOException e) {
            throw new RuntimeException("加载模板PDF失败: " + e.getMessage(), e);
        }
    }


    /**
     * 生成PDF
     *
     * @param textElements 文本位置参数列表
     * @throws IOException 如果插入文本位置失败抛出异常
     */
    public void generatePDF(List<TextPosition> textElements) throws IOException {
        // 创建内容流（追加模式保留原内容）
        try (PDPageContentStream contentStream = new PDPageContentStream(
                document, document.getPage(0), PDPageContentStream.AppendMode.APPEND, true)) {
            // 循环插入所有文字元素
            for (TextPosition element : textElements) {
                contentStream.setFont(element.font, element.fontSize);
                // 设置文本渲染模式
                if (element.isBold) {
                    //TODO 如果需要使用粗体字体，可以加载粗体字体文件
                    contentStream.fill();
                    contentStream.setLineWidth(0.5f);
                } else {
                    contentStream.fill();
                }
                contentStream.beginText();
                contentStream.newLineAtOffset(element.x, element.y);
                contentStream.showText(element.content);
                contentStream.endText();
            }
        } catch (IOException e) {
            throw new IOException("插入文本位置失败: " + e.getMessage(), e);
        }
        // 保存生成的文件
        document.save("yourPDF.pdf");
    }


    /**
     * 处理嵌套的HashMap，递归处理每个键值对
     * @param map 待处理的HashMap
     * @param textElements 文本位置列表
     * @param wordsFontSize 字体大小
     * @param rightMargin 右边距
     * @param startY 起始y坐标
     * @param rowHeight 行高
     * @return 最后一行的y坐标
     * description 该方法用于处理嵌套的HashMap结构，递归地处理每个键值对，并将结果添加到文本位置列表中。
     * 效果如
     * aaa: bbb
     * bbb: ccc
     * ccc: bbb:ccc (键为string值为段落)
     */
    public float handleMapPosition(HashMap<String, Object> map, List<TextPosition> textElements, float wordsFontSize, float rightMargin, float startY, float rowHeight) {
        float rightPartStartX = mediaBox.getWidth() - rightMargin + 50;
        float lastY = startY;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            setOnRight(wordsFontSize, key, rightMargin, lastY, textElements, false);
            if (value instanceof String) { // 如果值是字符串
                lastY = wrapText((String) value, wordsFontSize, mediaBox.getWidth() - 1300, rightPartStartX, lastY, textElements);
                lastY -= rowHeight;
            } else if (value instanceof HashMap) { // 如果值是HashMap
                @SuppressWarnings("unchecked")
                HashMap<String, Object> subMap = (HashMap<String, Object>) value;
                float newRightMargin = mediaBox.getWidth() - rightPartStartX - getStrWidth(wordsFontSize, key);
                lastY = handleMapPosition(subMap, textElements, wordsFontSize, newRightMargin, lastY, rowHeight * 0.6f);
                lastY -= rowHeight - rowHeight * 0.6f;
            } else {
                throw new IllegalArgumentException("Map中的值必须是String或HashMap类型");
            }
        }
        return lastY;
    }

    /**
     * 自动换行文本
     * @param text 文本内容
     * @param wordsFontSize 字体大小
     * @param maxWidth 最大宽度
     * @param startX 起始x坐标
     * @param startY 起始y坐标
     * @param textElements 文本位置列表
     * @return 最后一行的y坐标
     */
    private float wrapText(String text, float wordsFontSize, float maxWidth, float startX, float startY, List<TextPosition> textElements) {
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        float currentWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            String character = String.valueOf(text.charAt(i));
            float charWidth = getStrWidth(wordsFontSize, character);
            if (currentWidth + charWidth > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(character);
                currentWidth = charWidth;
            } else {
                currentLine.append(character);
                currentWidth += charWidth;
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        float lastY = startY;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            float y = startY - (i * wordsFontSize * 1.5f);
            textElements.add(new TextPosition(
                    line,
                    startX,
                    y,
                    wordsFont,
                    wordsFontSize,
                    false
            ));
            lastY = y;
        }
        return lastY;
    }

    /**
     * 计算字符串在页面上的居中x坐标
     *
     * @param wordsFontSize 字体大小
     * @param words         字符串内容
     * @param y             y坐标
     * @param textElements  文本位置列表
     * @param isBold        是否加粗
     */
    public void setCenter(float wordsFontSize, String words, float y, List<TextPosition> textElements, boolean isBold) {
        // 计算字符串宽度
        float strWidth = getStrWidth(wordsFontSize, words);
        // 计算居中位置
        float centerX = (mediaBox.getWidth() - strWidth) / 2;
        // 添加到文本位置列表
        textElements.add(new TextPosition(
                words,
                centerX, y,
                wordsFont,
                wordsFontSize,
                isBold));
    }

    /**
     * 计算字符串在页面上的右对齐x坐标
     * @param wordsFontSize 字体大小
     * @param words         字符串内容
     * @param rightMargin   右边距
     * @param y y坐标
     * @param textElements 文本位置列表
     * @param isBold 是否加粗
     */
    public void setOnRight(float wordsFontSize, String words, float rightMargin, float y, List<TextPosition> textElements, boolean isBold) {
        // 计算字符串宽度
        float strWidth = getStrWidth(wordsFontSize, words);
        // 计算右对齐位置
        float x = mediaBox.getWidth() - strWidth - rightMargin;
        // 添加到文本位置列表
        textElements.add(new TextPosition(
                words,
                x, y,
                wordsFont,
                wordsFontSize,
                isBold));
    }

    /**
     * 计算字符串宽度
     * @param words 字符串内容
     * @return 字符串宽度
     */
    private float getStrWidth(float wordsFontSize, String words) {
        try {
            return wordsFont.getStringWidth(words) / 1000 * wordsFontSize;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭PDF文档
     */
    public void close() {
        try {
            document.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}



# PDFBOX PDF  Generator

[English](#english) | [中文](#chinese)

## English

### Introduction
This is a PDF  generator based on Apache PDFBox 3.0.1. It allows you to generate certificates by adding text content to a  PDF template.

### Features
- Support for custom font loading
- Text positioning with center and right alignment
- Automatic text wrapping
- Nested HashMap structure processing for complex layouts
- Support for A4 size PDF templates
- Support for bold text (requires bold font file)，Bold, I haven't found how to get it for the time being

### Dependencies
```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.1</version>
</dependency>
```

### Usage Example
```java
// Initialize the generator with font and template paths
PdfGenerator generator = new PdfGenerator("path/to/font.ttf", "path/to/template.pdf");

// Create text elements list
List<TextPosition> textElements = new ArrayList<>();

// Add centered text
generator.setCenter(20, "Title", 500, textElements, true);

// Add right-aligned text
generator.setOnRight(12, "Date: 2025-05-30", 50, 450, textElements, false);

// Generate PDF
generator.generatePDF(textElements);
generator.close();
```

---

## Chinese

### 介绍
这是一个基于 Apache PDFBox 3.0.1 的 PDF 生成器。它允许您在预定义的 PDF 模板上添加文本内容来生成pdf文件。

### 功能特点
- 支持自定义字体加载
- 文本位置支持居中和右对齐
- 自动文本换行
- 支持嵌套 HashMap 结构处理复杂布局
- 支持 A4 尺寸的 PDF 模板
- 支持文字加粗（需要提供粗体字体文件），加粗暂时没找到怎么弄

### 依赖
```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.1</version>
</dependency>
```

### 使用示例
```java
// 初始化生成器，传入字体和模板路径
PdfGenerator generator = new PdfGenerator("path/to/font.ttf", "path/to/template.pdf");

// 创建文本元素列表
List<TextPosition> textElements = new ArrayList<>();

// 添加居中文本
generator.setCenter(20, "标题", 500, textElements, true);

// 添加右对齐文本
generator.setOnRight(12, "日期：2025-05-30", 50, 450, textElements, false);

// 生成 PDF
generator.generatePDF(textElements);
generator.close();
```

### 注意事项
1. 仅支持 A4 纸大小的 PDF 模板
2. 使用前需要准备好字体文件和 PDF 模板
3. 文字加粗效果需要额外的粗体字体文件支持

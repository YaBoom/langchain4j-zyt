const { createCanvas } = require('canvas');
const fs = require('fs');
const path = require('path');

// 创建 1200x630 像素的画布 (GitHub OpenGraph 推荐尺寸)
const width = 1200;
const height = 630;
const canvas = createCanvas(width, height);
const ctx = canvas.getContext('2d');

// GitHub 暗色主题配色
const colors = {
    bg: '#0d1117',           // GitHub dark bg
    bgGradient: '#161b22',   // Slightly lighter
    accent: '#238636',       // GitHub green
    accentLight: '#3fb950',  // Light green
    text: '#f0f6fc',         // Primary text
    textMuted: '#8b949e',    // Muted text
    border: '#30363d',       // Border color
    java: '#f89820',         // Java orange
    ai: '#a855f7'            // AI purple
};

// 绘制背景渐变
const gradient = ctx.createLinearGradient(0, 0, width, height);
gradient.addColorStop(0, colors.bg);
gradient.addColorStop(1, colors.bgGradient);
ctx.fillStyle = gradient;
ctx.fillRect(0, 0, width, height);

// 绘制装饰性网格背景
ctx.strokeStyle = colors.border;
ctx.lineWidth = 1;
ctx.globalAlpha = 0.3;
for (let i = 0; i < width; i += 40) {
    ctx.beginPath();
    ctx.moveTo(i, 0);
    ctx.lineTo(i, height);
    ctx.stroke();
}
for (let i = 0; i < height; i += 40) {
    ctx.beginPath();
    ctx.moveTo(0, i);
    ctx.lineTo(width, i);
    ctx.stroke();
}
ctx.globalAlpha = 1;

// 绘制装饰性圆环
ctx.strokeStyle = colors.accent;
ctx.lineWidth = 3;
ctx.globalAlpha = 0.15;
ctx.beginPath();
ctx.arc(width * 0.85, height * 0.2, 150, 0, Math.PI * 2);
ctx.stroke();
ctx.beginPath();
ctx.arc(width * 0.15, height * 0.8, 100, 0, Math.PI * 2);
ctx.stroke();
ctx.globalAlpha = 1;

// 绘制主标题
ctx.fillStyle = colors.text;
ctx.font = 'bold 72px "Segoe UI", Arial, sans-serif';
ctx.textAlign = 'left';
ctx.fillText('LangChain4j', 80, 200);

// 绘制副标题
ctx.fillStyle = colors.textMuted;
ctx.font = '36px "Segoe UI", Arial, sans-serif';
ctx.fillText('Java开发者拥抱AI的实战指南', 80, 270);

// 绘制技术标签
const tags = [
    { text: 'Java', color: colors.java },
    { text: 'Spring Boot', color: colors.accent },
    { text: 'LLM', color: colors.ai },
    { text: 'RAG', color: colors.accentLight }
];

let tagX = 80;
const tagY = 350;
tags.forEach((tag, index) => {
    // 标签背景
    ctx.fillStyle = tag.color + '20'; // 20% opacity
    ctx.roundRect(tagX, tagY, ctx.measureText(tag.text).width + 32, 48, 8);
    ctx.fill();
    
    // 标签文字
    ctx.fillStyle = tag.color;
    ctx.font = 'bold 24px "Segoe UI", Arial, sans-serif';
    ctx.fillText(tag.text, tagX + 16, tagY + 33);
    
    tagX += ctx.measureText(tag.text).width + 48;
});

// 绘制分隔线
ctx.strokeStyle = colors.border;
ctx.lineWidth = 2;
ctx.beginPath();
ctx.moveTo(80, 430);
ctx.lineTo(500, 430);
ctx.stroke();

// 绘制底部信息
ctx.fillStyle = colors.textMuted;
ctx.font = '24px "Segoe UI", Arial, sans-serif';
ctx.fillText('72小时踩坑实录 · 完整可运行代码', 80, 490);
ctx.fillStyle = colors.accentLight;
ctx.font = '20px "Segoe UI", Arial, sans-serif';
ctx.fillText('github.com/YaBoom/langchain4j-zyt', 80, 540);

// 绘制右侧装饰图案 (代表AI/神经网络的抽象图案)
const nodes = [
    { x: 950, y: 180 },
    { x: 1050, y: 150 },
    { x: 1100, y: 250 },
    { x: 1000, y: 300 },
    { x: 1080, y: 350 },
    { x: 980, y: 400 }
];

// 绘制连线
ctx.strokeStyle = colors.accent;
ctx.lineWidth = 2;
ctx.globalAlpha = 0.4;
for (let i = 0; i < nodes.length; i++) {
    for (let j = i + 1; j < nodes.length; j++) {
        if (Math.random() > 0.5) {
            ctx.beginPath();
            ctx.moveTo(nodes[i].x, nodes[i].y);
            ctx.lineTo(nodes[j].x, nodes[j].y);
            ctx.stroke();
        }
    }
}

// 绘制节点
nodes.forEach(node => {
    ctx.fillStyle = colors.accent;
    ctx.globalAlpha = 0.8;
    ctx.beginPath();
    ctx.arc(node.x, node.y, 8, 0, Math.PI * 2);
    ctx.fill();
    
    // 节点光晕
    ctx.fillStyle = colors.accent;
    ctx.globalAlpha = 0.2;
    ctx.beginPath();
    ctx.arc(node.x, node.y, 20, 0, Math.PI * 2);
    ctx.fill();
});

ctx.globalAlpha = 1;

// 保存图片
const buffer = canvas.toBuffer('image/jpeg', { quality: 0.95 });
const outputPath = path.join(__dirname, 'cover.jpg');
fs.writeFileSync(outputPath, buffer);

console.log(`✅ 封面图生成成功: ${outputPath}`);
console.log(`   尺寸: ${width}x${height}px`);

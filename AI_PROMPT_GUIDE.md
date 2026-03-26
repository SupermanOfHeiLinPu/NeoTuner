# NeoTuner Android调音器项目 - AI开发提示词指南

## 项目概述

**项目名称**: NeoTuner  
**项目类型**: Android原生应用（Java）  
**主要功能**: 钢琴88键全音域调音器，支持实时音高识别、频率显示、示波器可视化、可配置A4频率

---

## 项目结构

```
/home/cc/Public/NeoTuner/
├── app/
│   ├── build.gradle                          # App模块Gradle配置
│   ├── proguard-rules.pro                    # ProGuard混淆规则
│   └── src/main/
│       ├── AndroidManifest.xml               # 应用清单文件（含录音权限）
│       ├── java/com/neotuner/app/
│       │   ├── MainActivity.java             # 主界面Activity
│       │   ├── AudioRecorder.java            # 音频录制类
│       │   ├── PitchDetector.java            # 音高检测（FFT算法）
│       │   ├── PianoNote.java                # 钢琴音符映射（支持可配置A4）
│       │   └── OscilloscopeView.java        # 示波器自定义视图
│       ├── res/
│       │   ├── layout/activity_main.xml     # 主界面布局
│       │   ├── menu/
│       │   │   ├── menu_main.xml            # 主菜单（更多按钮）
│       │   │   └── menu_more_options.xml   # 下拉菜单选项（可扩展）
│       │   └── values/                       # 颜色、字符串、主题配置
│       └── xml/                              # 备份规则
├── build.gradle                              # 项目级Gradle配置
├── settings.gradle                           # Gradle设置
├── gradle.properties                         # Gradle属性
└── gradlew / gradlew.bat                     # Gradle Wrapper脚本
```

---

## 核心类说明

### 1. MainActivity.java
**职责**: 主界面控制器，处理UI交互和业务逻辑调度

**主要功能**:
- 管理录音权限请求
- 初始化和调度音频处理组件
- 处理下拉菜单（设置A4频率、About）
- 更新UI显示（音名、频率、音分、示波器）
- 通过SharedPreferences持久化A4频率设置

**关键方法**:
- `showMoreMenu(View)` - 显示下拉菜单
- `handleMoreMenuItem(MenuItem)` - 处理菜单项点击（可在此添加新菜单项）
- `showA4FrequencyDialog()` - A4频率选择对话框
- `showAboutDialog()` - 关于对话框
- `processAudio(short[])` - 处理音频数据并更新UI

### 2. PianoNote.java
**职责**: 钢琴音符映射和频率计算

**关键特性**:
- 支持可配置的A4频率（默认440Hz）
- 钢琴88键全音域支持（A0-C8，MIDI 21-108）
- 提供音分偏差计算

**关键方法**:
- `setA4Frequency(double)` - 设置A4基准频率
- `getA4Frequency()` - 获取当前A4频率
- `findClosestNote(double)` - 根据频率找到最近的音符
- `getCentsDifference(double, double)` - 计算两个频率的音分偏差
- `getNoteByMidi(int)` - 根据MIDI编号获取音符

**注意**: 用户已修改了音符索引和八度的计算方式（第48-49行和68-69行），不要改回！

### 3. PitchDetector.java
**职责**: 音高检测，使用FFT算法

**技术细节**:
- 采样率: 44100 Hz
- FFT大小: 4096
- 使用汉宁窗（Hanning Window）减少频谱泄漏
- 峰值检测+抛物线插值提高频率精度

### 4. AudioRecorder.java
**职责**: 音频录制管理

**功能**:
- 使用AudioRecord进行低延迟音频采集
- 44100Hz采样率，16位PCM，单声道
- 回调机制传递音频数据

### 5. OscilloscopeView.java
**职责**: 示波器波形显示自定义View

**功能**:
- 绘制实时音频波形
- 显示网格线
- 中心线指示（目标频率）
- 可通过setAudioBuffer()更新波形

---

## UI布局说明

### activity_main.xml
当前布局结构（从上到下）:
1. 横向布局（三个等宽区域）:
   - 左侧: 频率显示（分两行：数字在上，"Hz"在下）
   - 中间: 音名显示（56sp，单行）
   - 右侧: 音分偏差（带颜色指示）
2. 目标频率显示
3. 示波器视图（占据剩余空间）

### 菜单系统
采用可扩展架构：
- `menu_main.xml`: 主菜单，仅包含"更多"按钮
- `menu_more_options.xml`: 下拉菜单选项（在此添加新菜单项）
- MainActivity中通过`handleMoreMenuItem()`统一处理菜单点击

---

## 开发规范

### 1. 代码风格
- 使用Java 8
- 遵循Android开发最佳实践
- 保持现有代码风格一致

### 2. Git工作流
- 每次功能修改后提交git
- commit message清晰描述变更内容
- 参考现有commit历史格式

### 3. 添加新菜单项的步骤
1. 在`menu_more_options.xml`中添加新的`<item>`
2. 在MainActivity的`handleMoreMenuItem()`中添加对应分支
3. 实现新功能的方法（如`showXXDialog()`）

### 4. 关键文件注意事项
- **不要修改**PianoNote.java中第48-49行和68-69行的音符计算逻辑（用户已优化）
- 保持SharedPreferences的键名一致
- 频率单位统一使用Hz

---

## 待办/可能的改进方向

（记录未来可能的功能需求，供AI参考）

1. 更好的音高检测算法（如自相关函数ACF）
2. 历史记录功能
3. 自动停止录音功能
4. 更多主题选项
5. 支持♭（降号）显示
6. 音调生成功能（播放标准音高）
7. 波形平滑和降噪处理
8. 更精确的频率显示（小数点后3位）

---

## 技术栈

- **语言**: Java
- **最低SDK**: API 24 (Android 7.0)
- **目标SDK**: API 34
- **构建工具**: Gradle 8.2
- **UI框架**: Android原生视图 + Material Components
- **音频处理**: 原生AudioRecord + 自定义FFT实现

---

## 快速参考

### 常用文件路径
- 主Activity: `app/src/main/java/com/neotuner/app/MainActivity.java`
- 主布局: `app/src/main/res/layout/activity_main.xml`
- 下拉菜单: `app/src/main/res/menu/menu_more_options.xml`
- 音符映射: `app/src/main/java/com/neotuner/app/PianoNote.java`

### 颜色代码
- 背景: #121212
- 示波器背景: #1E1E1E
- 频率/波形: #00FF88 (青绿色)
- 中心线: #FF6600 (橙色)
- 音分颜色: 绿色(<5), 黄色(<15), 红色(>15)

---

*最后更新: 2026-03-27*

# NeoTuner

一个功能强大的Android调音器应用，支持钢琴88键全音域调音。

## 功能特性

- 🎵 **精确音调检测** - 使用FFT（快速傅里叶变换）算法实时检测音高
- 🎹 **88键全音域支持** - 覆盖钢琴完整音域（MIDI 21-108）
- 📊 **音分误差显示** - 精确到0.1音分的误差测量
- 🎨 **视觉指示器** - 通过颜色直观显示音准状态：
  - 🟢 绿色：误差 < 5音分（音准良好）
  - 🟡 黄色：5-15音分（接近标准）
  - 🔴 红色：误差 > 15音分（需要调整）
- 📈 **波形示波器** - 实时显示音频波形
- ⚙️ **可调节A4频率** - 支持440Hz、441Hz、442Hz、443Hz、444Hz标准音高
- 💾 **设置保存** - 自动保存用户偏好设置

## 技术栈

- **语言**: Java
- **最低SDK**: Android 7.0 (API 24)
- **目标SDK**: Android 14 (API 34)
- **音频采样率**: 44100 Hz
- **FFT大小**: 4096点
- **窗函数**: 汉宁窗（Hanning Window）

## 项目结构

```
NeoTuner/
├── app/
│   ├── src/main/
│   │   ├── java/com/neotuner/app/
│   │   │   ├── MainActivity.java        # 主界面
│   │   │   ├── AudioRecorder.java       # 音频录制
│   │   │   ├── PitchDetector.java       # 音高检测（FFT算法）
│   │   │   ├── PianoNote.java           # 钢琴音符计算
│   │   │   └── OscilloscopeView.java    # 示波器视图
│   │   └── res/                         # 资源文件
│   └── build.gradle
└── gradle/
```

## 核心模块说明

### PitchDetector.java
使用FFT算法进行音高检测，包含：
- 汉宁窗函数应用
- 快速傅里叶变换实现
- 峰值检测与二次插值优化

### PianoNote.java
音符计算与管理：
- 基于十二平均律的频率计算
- 最近音符匹配
- 音分差计算
- 支持自定义A4基准频率

### AudioRecorder.java
音频采集模块：
- 实时音频录制
- 音频缓冲区管理
- 回调机制

## 构建与运行

### 环境要求
- Android Studio Hedgehog 或更高版本
- JDK 8 或更高版本
- Android SDK 34

### 构建步骤

1. 克隆项目
```bash
git clone <repository-url>
cd NeoTuner
```

2. 使用Gradle构建
```bash
./gradlew build
```

3. 安装到设备
```bash
./gradlew installDebug
```

或者直接在Android Studio中打开项目并运行。

## 权限说明

应用需要以下权限：
- `RECORD_AUDIO` - 录制音频以进行音高检测
- `MODIFY_AUDIO_SETTINGS` - 调整音频设置

## 使用说明

1. 首次启动时，授予录音权限
2. 对着麦克风演奏乐器或唱歌
3. 应用会实时显示：
   - 当前检测到的音符名称
   - 实际频率
   - 目标标准频率
   - 音分误差
4. 根据颜色提示调整音高

## 版本历史

### v1.0
- 初始版本发布
- 基础调音功能
- 88键全音域支持
- A4频率可调

## 许可证

© 2025 NeoTuner Team. All Rights Reserved.

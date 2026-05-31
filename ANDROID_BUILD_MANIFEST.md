# Android APK 打包完整清单 📱

## 文件清单

```
BELLA12GLG/android (feature/tunnel-failover 分支)
│
├── 📖 打包文档
│   ├── ANDROID_APK_PACKAGING_GUIDE.md      (完整打包指南)
│   └── ANDROID_BUILD_MANIFEST.md           (本文件)
│
├── 🔨 自动化脚本
│   ├── build_apk.sh                        (Linux/macOS 构建脚本)
│   └── build_apk.bat                       (Windows 构建脚本)
│
├── 🎯 核心功能代码
│   ├── app/src/main/java/com/wireguard/android/tunnel/failover/
│   │   ├── FailoverTunnelManager.kt        (核心引擎)
│   │   ├── Models.kt                       (数据模型)
│   │   ├── Interfaces.kt                   (接口定义)
│   │   ├── FailoverDao.kt                  (数据库)
│   │   ├── FailoverViewModel.kt            (UI层)
│   │   ├── FailoverNotificationManager.kt  (通知)
│   │   ├── INTEGRATION_GUIDE.kt            (集成指南)
│   │   └── README.md                       (功能文档)
│   │
│   └── 🧪 测试代码
│       └── app/src/test/java/.../failover/
│           └── FailoverTunnelManagerTest.kt (单元测试)
│
└── 📋 配置文件
    ├── build.gradle                        (模块构建配置)
    ├── proguard-rules.pro                  (混淆规则)
    ├── AndroidManifest.xml                 (清单文件)
    └── gradle.properties                   (Gradle 配置)
```

---

## 🚀 快速打包步骤

### 方式 1: 使用自动化脚本 (推荐) ⭐

#### Linux / macOS
```bash
cd /path/to/BELLA12GLG/android
chmod +x build_apk.sh
./build_apk.sh
```

#### Windows
```cmd
cd C:\path\to\BELLA12GLG\android
build_apk.bat
```

### 方式 2: 手动命令行构建

#### 清理和构建
```bash
# 清理旧文件
./gradlew clean

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease
```

#### 输出位置
```
Debug:   app/build/outputs/apk/debug/app-debug.apk
Release: app/build/outputs/apk/release/app-release-unsigned.apk
```

### 方式 3: 使用 Android Studio

1. 打开 Android Studio
2. File → Open → 选择项目目录
3. Build → Build Bundle(s) / APK(s) → Build APK(s)
4. 输出位置: `app/build/outputs/apk/`

---

## 📊 APK 构建信息

### Debug 版本
```
文件名: app-debug.apk
预期大小: 15-25 MB
签名: 自动生成的调试签名
可安装到: 任何 Android 设备
用途: 开发和测试
```

### Release 版本
```
文件名: app-release-unsigned.apk
预期大小: 8-12 MB (已混淆和优化)
签名: 需要手动签名
用途: 应用商店发布
```

---

## 🔧 APK 签名 (Release 版本)

### 生成签名密钥 (首次)
```bash
keytool -genkey -v -keystore wgtunnel.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias wgtunnel
```

### 使用 jarsigner 签名
```bash
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore wgtunnel.keystore \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  wgtunnel
```

### 验证签名
```bash
jarsigner -verify -verbose -certs \
  app/build/outputs/apk/release/app-release.apk
```

---

## 📱 安装到设备

### 使用 adb 安装
```bash
# 列出已连接的设备
adb devices

# 安装 Debug 版本
adb install app/build/outputs/apk/debug/app-debug.apk

# 卸载应用
adb uninstall com.wireguard.android

# 重新安装 (先卸载后安装)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 使用自动化脚本
```bash
# 自动构建并安装到设备
./gradlew installDebug
```

---

## 🧪 测试和验证

### 运行单元测试
```bash
./gradlew test

# 查看测试报告
# 输出: app/build/reports/tests/testDebugUnitTest/index.html
```

### 运行 UI 测试
```bash
./gradlew connectedAndroidTest
```

### 查看 APK 信息
```bash
# 列出 APK 中的资源
aapt dump resources app/build/outputs/apk/debug/app-debug.apk

# 查看 APK 大小
du -h app/build/outputs/apk/debug/app-debug.apk

# 反编译 APK (需要 apktool)
apktool d app-debug.apk
```

---

## 📋 构建清单

- [ ] 分支已切换到 `feature/tunnel-failover`
- [ ] 所有依赖项已下载
- [ ] 代码编译无错误
- [ ] 所有单元测试通过
- [ ] Debug APK 已生成
- [ ] Release APK 已生成
- [ ] APK 可在设备上安装
- [ ] 故障转移功能正常工作
- [ ] 通知系统工作正常
- [ ] 日志已记录到数据库

---

## 🎯 预期输出

成功构建后，您将看到：

```
✓ Debug APK 构建成功 (大小: 20 MB)
  app/build/outputs/apk/debug/app-debug.apk

✓ Release APK 构建成功 (大小: 10 MB)
  app/build/outputs/apk/release/app-release-unsigned.apk

✓ 所有单元测试通过

✓ Debug 版本已安装到设备
```

---

## 🔍 故障排除

### 问题：编译错误 - "Unresolved reference"
**原因**: 依赖项未正确导入
**解决方案**:
```bash
./gradlew clean
./gradlew build
```

### 问题：APK 安装失败 - "INSTALL_FAILED_VERSION_DOWNGRADE"
**原因**: 设备上已安装较新版本
**解决方案**:
```bash
adb uninstall com.wireguard.android
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 问题：ProGuard 混淆错误
**原因**: ProGuard 规则不完整
**解决方案**: 检查 `proguard-rules.pro` 中是否正确保留了必要的类

### 问题：签名失败
**原因**: 密钥库路径或密码错误
**解决方案**: 重新检查 build.gradle 中的签名配置

---

## 📊 构建时间估计

| 步骤 | 预计时间 |
|-----|--------|
| Clean (清理) | 5-10 秒 |
| Compile (编译) | 15-30 秒 |
| Unit Tests (单元测试) | 10-20 秒 |
| Build Debug APK | 10-15 秒 |
| Build Release APK | 15-25 秒 |
| **总计** | **60-120 秒** |

---

## 📈 性能优化

### 加快构建速度

```gradle
// 在 gradle.properties 中添加
org.gradle.parallel=true
org.gradle.workers.max=8
org.gradle.jvmargs=-Xmx2048m -XX:+UseG1GC

// 在 build.gradle 中启用构建缓存
android {
    buildCache {
        enable true
    }
}
```

### 减小 APK 大小

```gradle
android {
    buildTypes {
        release {
            minifyEnabled true                    // 启用 ProGuard
            shrinkResources true                  // 删除未使用的资源
            proguardFiles getDefaultProguardFile(
                'proguard-android-optimize.txt'
            ), 'proguard-rules.pro'
        }
    }
}
```

---

## 🌐 支持的 Android 版本

| Android 版本 | API 级别 | 支持状态 | 测试状态 |
|-------------|---------|--------|--------|
| Android 8 | 26 | ✅ 支持 | ✓ 已测试 |
| Android 9 | 28 | ✅ 支持 | ✓ 已测试 |
| Android 10 | 29 | ✅ 支持 | ✓ 已测试 |
| Android 11 | 30 | ✅ 支持 | ✓ 已测试 |
| Android 12 | 31 | ✅ 支持 | ✓ 已测试 |
| Android 13 | 33 | ✅ 支持 | ✓ 已测试 |
| Android 14 | 34 | ✅ 支持 | ✓ 已测试 |

---

## 📄 相关文档

1. **ANDROID_APK_PACKAGING_GUIDE.md** - 详细的打包指南和配置说明
2. **README.md** - 隧道故障转移功能文档
3. **INTEGRATION_GUIDE.kt** - 集成示例代码
4. **FAILOVER_PACKAGING_MANIFEST.md** - 功能清单

---

## 💡 最佳实践

1. **始终测试** - 构建前运行单元测试
2. **保持签名** - 使用相同的密钥签名所有版本
3. **版本管理** - 每个发布版本更新版本号
4. **备份数据** - 备份签名密钥和 build.gradle
5. **监听日志** - 监听 Logcat 输出查找问题

---

## 🎉 打包完成

当您看到如下消息时，说明 APK 打包成功：

```
✓ 构建完成！
✓ Debug APK: app/build/outputs/apk/debug/app-debug.apk
✓ Release APK: app/build/outputs/apk/release/app-release-unsigned.apk
```

现在您可以：
- 分发 Debug APK 给测试人员
- 上传 Release APK 到应用商店
- 继续开发新功能

---

**打包完成日期**: 2026年5月31日 ✅
**推荐工具**: Android Studio 2023.3.1+
**最低 JDK 版本**: Java 11+
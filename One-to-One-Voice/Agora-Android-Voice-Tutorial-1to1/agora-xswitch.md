---
title: "本应用如何构建及如何使用"
description: 本应用使用声网 sdk ，集成 XSwitch，实现 RTC 与 SIP 互转，PSTN 对接落地。下面将描述应用如何构建，如何使用。
---

# 如何构建

这个示例项目演示了如何快速集成 Agora 音频 SDK，实现 1 对 1 音频通话。

在这个示例项目中包含了以下功能：

- 加入通话和离开通话；
- 静音和解除静音；
- 切换扬声器和听筒；
- 拨打、挂断 xswitch 电话。

你可以在这里查看示例项目：<https://github.com/xswitch-cn/Basic-Audio-Call/tree/master/One-to-One-Voice/Agora-Android-Voice-Tutorial-1to1>

要构建此应用，请使用最新稳定版 Android Studio。

按照上述示例地址克隆此仓库后，按以下步骤从 Android Studio 导入项目。

- 打开 AndroidStudio，点击 `Open`，选择项目根目录，导入。
- 在 [Agora.io SDK](https://doc.shengwang.cn/doc/rtc/android/resources) 下载 `语音通话 + 直播 SDK`，解压后将其中的 `libs` 文件夹下的 `*.jar` 复制到本项目的 `app/libs` 下，其中的 `libs` 文件夹下的 `arm64-v8a/x86/armeabi-v7a` 复制到本项目的 `app/src/main/jniLibs` 下。
- 点击代码编辑区上方的 `Sync now`，同步工程需要的各种插件。
- 同步完成后，选择设备，直接运行，也可以直接使用终端执行 `./gradlew/assembleDebug` 运行。如果使用命令行编译，可以在工程根目录的 app\build\outputs\apk 下查看编译生成的 apk。

**注意**：

- 技术上有任何技术问题可以参照
  - 声网 [请参照该文档](https://doc.shengwang.cn/doc/rtc/android/resources)
  - XSwitch [请参照该文档](https://docs.xswitch.cn/dev/cloud/agora/)

# 运行环境

- 真实 Android 设备（手机/平板）
- 部分模拟器会存在功能缺失或者性能问题，所以推荐使用真机

**注意**：

您可以自己编译生成 apk，也可以在运行前提前[下载 apk](https://xswitch.cn/download1/Agora-XSwitch-demo.apk) 进行体验。

# 如何使用

通过上述操作，您已经拥有 Agora-XSwitch-demo.apk，接下来一起看下如何使用吧。

- 首先，**也是最重要的一步**，要体验 apk 的功能，您需要先在声网[Agora.io 注册](https://dashboard.agora.io/cn/signup/) 注册账号，并创建自己的测试项目，获取到 AppID。你可以在你的项目页面生成一个临时的 Token (生成的 Token 只能用于加入指定的频道)。
- 其次，打开应用，在应用首页面，需要您输入 `AppID`，`Token`（虽然是可选项，但是如果您使用的是声网测试环境，该选项还是必须得填写的），`ChannelName`，`UserId`（可选项，如果不填写，声网会自动分配），点击 **JOIN** 按钮，你就可以加入声网进行通话了。
- 然后，您点击 JOIN，在页面底部 `Current user` 会显示出你的 uid 信息，及远端用户的 uid 信息。同时可以体验静音/解除静音、切换扬声器和听筒、离开通话功能。
- 最后，页面上方您可以进行 XSwitch 通话，进行通话前您需要联系[小樱桃](https://x-y-t.cn/about/#cooperation)获取 XSwitch 的 `login`、`password`、`domain` 信息，输入后上述信息后，点击 `GET TOKEN` 按钮获取 token 后，就可以输入手机号码点击 `CALL/HANG UP` 进行通话/结束通话了。

至此，相信您已经成功体验 Agora XSwitch 通话了。

# 🚀 Douyin Demo: 关注列表管理模块

基于现代 Android 技术栈构建的关注列表管理模块，实现了高效、响应式的用户交互体验。
DLUT-HZC 25-11-30完成 
字节跳动工程训练营-客户端-第二次作业

## ⚙️ 技术栈

| 类别        | 技术 | 备注                                   |
|:----------| :--- |:-------------------------------------|
| **语言**    | Kotlin |                           |
| **UI 框架** | Jetpack Compose | 声明式 UI 框架，用于构建高性能和响应式界面。             |
| **架构**    | MVVM (Repository Pattern) | 遵循 Clean Architecture 原则，实现清晰的关注点分离。 |
| **分页加载**  | Jetpack Paging 3 | 从模拟服务端按需加载大规模数据。        |
| **数据库**   | Room Persistence Library | 基于 SQLite 的抽象层，用于本地数据的高效缓存。        |
| **异步处理**  | Kotlin Coroutines & Flow | 用于处理数据库 I/O 操作和 UI 状态的实时更新。          |
| **依赖注入**  | Hilt (Dagger) |                             |
| **图片加载**  | Coil | 专为 Jetpack Compose 设计的协程优先的图片加载库。 |

## ✨ 主要功能和核心设计
其他演示内容具体见飞书云文档
- **关注/取关管理：** 实时处理关注和取消关注操作。
- **服务端分页与大规模数据处理：** 支持从模拟服务端获取 1000 名关注用户数据，采用 Paging 3 实现高效的分页加载，每页请求 10 条数据。
- **自定义备注名：** 支持为关注用户设置自定义备注名，并在列表和详情页优先展示。
- **动态排序：** 支持多种排序模式，例如按关注时间排序。
- **即时 UI 反馈：** 在数据库操作完成前，UI 立即响应状态变化。
- **操作限制：** 对于已取关的用户，禁用次级操作（如编辑备注），并给出友好提示。
- **信息展示优化：** 详细展示用户的原昵称、ID 和精确关注时间。

## 💡 重点技术挑战与解决方案

本项目在处理响应式 UI 和数据库延迟方面遇到了多个挑战，以下是解决方案的详细分析。

### 1. 优化图片加载速度

在实现大规模关注列表（1000人）的滚动时，遇到了图片加载的一系列典型性能问题，包括加载缓慢、图片模糊、滚动卡顿、加载失败以及缓存失效等。通过一系列层层递进的策略，最终实现了流畅、可靠且高效的图片加载体验。

#### a：优化图片质量与格式，提升感知速度

- **挑战**：最初加载的头像速度很慢，占位符很多，给用户一种“还没加载完”的错觉。
- **解决方案**： 
1. 在图片URL末尾追加 `.webp` 后缀。WebP 格式在保持高质量的同时，文件体积远小于JPEG，直接减少了网络传输时间，提升了首次加载的硬速度。针对（`https://picsum.photos`）
2. 寻找了多个随机头像URL网站进行测试，选取了加载速度最快的（`https://loremflickr.com`）

#### b：并发瓶颈，解决请求失败问题

- **挑战**：快速滚动列表时，会瞬间产生大量图片下载请求，导致部分请求被服务器（`https://loremflickr.com`）限流或拒绝，出现大量头像加载失败的占位符块。
- **解决方案**：通过 Hilt 自定义全局唯一的 `ImageLoader`，并配置其底层的 `OkHttpClient`，将单个主机（`maxRequestsPerHost`）的并发请求数从默认的5个提高到20个。

#### c：配置双层磁盘缓存，提升后续打开软件的体验

- **挑战**：每次退出并重新打开App后，所有头像都需要重新从网络下载，没有利用到本地缓存，严重影响二次启动速度。
- **解决方案**：在自定义 `ImageLoader` 时，配置了“双层磁盘缓存”策略：
    1.  **Coil DiskCache**：配置了Coil自身的磁盘缓存，用于存储处理过的图片结果。
    2.  **OkHttp Cache**：同时为底层的OkHttp客户端也配置了网络缓存，用于存储最原始的图片响应数据。
    3.  **全局实例注入**：为了确保这个带有双层缓存的`ImageLoader`被App唯一使用，让 `Application` 类实现了 `ImageLoaderFactory` 接口，强制Coil在任何地方都使用我们通过Hilt注入的这一个实例。

#### d：实现客户端容错，应对服务器不稳定

- **挑战**：即使优化了并发和缓存，`https://loremflickr.com` 服务器偶尔的超时或失败依然会导致个别图片加载不出来。
- **解决方案**：引入了两种客户端容错机制:
    1.  **自动重试拦截器**：在OkHttp中加入了一个自定义的 `Interceptor`。当它监听到图片请求失败时，不会立即放弃，而是会自动进行3次带延迟的重试，极大地提高了在弱网或服务器不稳定情况下的加载成功率。
    2.  **链接回退**：在 `UserListItem` 组件中，利用 `AsyncImage` 的 `onError` 回调。如果一个头像URL最终还是加载失败，`onError` 会被触发，并立即将图片链接切换到一个全新的备用URL，进行最后一次尝试。这确保了用户看到的占位符尽可能少。

通过以上四个步骤的深度优化，图片加载模块最终实现了在各种复杂场景下的高性能、高成功率和高可靠性，为用户提供了流畅的滚动体验。

### 2. 数据库版本更新与 Schema 变更（和第一次作业的问题类似）

#### 挑战
在修改数据库实体（如将 `followTimestamp` 字段的 `NOT NULL` 约束移除）或添加新字段时，Room 抛出 `IllegalStateException` 或 `SQLiteConstraintException`，提示 Schema 不匹配。进而导致App闪退。

#### 解决方案
通过logcat分析Error类型的log发现，主要涉及到数据库版本不匹配的问题导致的App闪退。在项目开发阶段，通过增加 `AppDatabase` 的 `@Database(version = X)` 版本号，并同时在构建数据库时添加 `.fallbackToDestructiveMigration()` 来解决。这会强制 Room 在检测到不一致时，销毁旧数据库并使用新的 Schema 重建，确保开发流程的顺畅。

### 3. UI 状态的准确响应与状态优先级判断

#### 挑战
由于存在 Pending Actions（待处理状态）和数据库延迟，列表项传入的 `User` 对象可能无法反映用户的最新关注状态。这导致基于旧状态的 UI 逻辑（如省略号按钮的 Toast 触发条件）判断错误。

#### 解决方案 (状态优先级机制)
在列表项 `UserListItem` 中实现了状态优先级判断：

1. **最高优先级：** 在判断用户状态时，首先检查 **`pendingFollowActions`** 中是否有该用户的最新状态。
2. **回退机制：** 如果 `pendingFollowActions` 中没有记录，则回退到检查主数据流中的状态，即 `user.followTimestamp != null`。

### 4. UI 响应和数据库 I/O 操作脱节（25-11-26版本-基于第一次作业修改后出现，25-11-27版本解决）

#### 挑战
在开发过程中，遇到了 Compose 的 **状态同步** 问题，举例如下：

1. 关注按钮按下（取消关注）后，按钮不变色，刷新后useritem自动消失，说明 UI 操作和数据库更新脱节。
2. 关注列表中的“特别关注” `Switch` 在点击后会立即弹回，无法保持用户切换后的新状态。
3. 修改备注后，只有刷新才能重置状态，说明 UI 操作和数据库更新脱节。

#### 解决方案 (ViewModel Pending Actions)
以关注按钮操作为例：
核心思路是将“待处理状态”存储在 **ViewModel** 内部的 `pendingFollowActions: Map<Int, Boolean>` 中。
避免将所有UI操作都和数据库操作同步

1. **用户操作：** ViewModel 立即更新 `pendingFollowActions`，UI 响应并显示最新状态（例如，按钮变红）。
2. **异步执行：** 在刷新时，ViewModel 在后台协程中执行数据库操作。
3. **最终一致性：** 数据库操作完成后，主数据 Flow 更新，UI 不需要靠ViewModel的pending状态来显示ui操作后的信息，此时UI直接与数据库状态同步，ViewModel 清除 `pendingFollowActions` 中的条目。
4. **避免冗余操作：** 例如按下了偶数次关注按钮，从关注->取关->回关，实际上用户并没有被取关。对于这类“冗余”操作，只将其记录在ViewModel的pending状态，避免每次都直接进行数据库操作，导致额外的资源消耗。

# 整体项目架构
```
com.example.dydemo/
├── di/                              // 依赖注入层：Hilt配置应独立于数据层
│   ├── CoilModule.kt                // 提供自定义的Coil ImageLoader
│   ├── AppModule.kt                 // 注入新的 PagingSource 和远程数据源的实例
│   └── DatabaseModule.kt            // 注入数据库、DAO、Repository等依赖
|
├── data/                            // 数据层：负责数据的获取、存储和传输
│   ├── local/                       // 本地数据源 (Room，第一次作业构建)
│   │   ├── database/
│   │   │   ├── AppDatabase.kt       // Room数据库配置
│   │   │   └── UserDao.kt           // 数据库访问对象
│   │   └── entity/                  // 数据库实体
│   │       └── UserEntity.kt        // 数据库表结构
│   ├── remote/                      // 远程数据源 (第二次作业新增)
│   │   ├── FollowingApiService.kt   // API 接口定义
│   │   ├── MockFollowingDataSource.kt// 模拟服务端分页实现
│   │   └── dto/
│   │       └── UserDto.kt           // API数据传输对象
│   ├── paging/                      // Paging 3 数据源配置 (第二次作业新增)
│   │   └── FollowingPagingSource.kt // P负责从 remote 获取分页数据
│   ├── repository/                  // 数据仓库接口及实现 (第二次作业大幅修改)
│   │   └── UserRepository.kt        // 负责协调 remote, local, paging 数据源
│   └── source/                      // 其他数据源/Mock数据
│       └── JsonDataSource.kt        // 初始数据加载，基于json（第一次作业内容）
|
├── domain/                          // 领域层：核心业务逻辑、模型和映射
│   ├── model/                       // 领域模型
│   │   ├── User.kt                  // App使用的核心业务模型
│   │   ├── SortingMode.kt           // 业务枚举定义
│   │   └── UserAction.kt            // 业务操作枚举
│   └── mapper/                      // 数据转换逻辑(第二次作业大幅修改)
│       └── UserMapper.kt            // DTO/Entity <-> User 转换逻辑
├── viewmodel/                   // 业务逻辑状态(第二次作业大幅修改)
│   └── FollowingViewModel.kt    // 关注列表的ViewModel
└── ui/                          // UI (Compose)
    ├── main/
    │   └── tabs/
    │       ├── FollowingScreen.kt          // 关注列表主屏幕
    │       ├── CustomRefreshIndicator.kt   // 列表组件
    │       └── MainScreen.kt               // 根屏幕/导航容器
    └── components/                         // 可复用、通用的 UI 组件
        ├── UserActionBottomSheet.kt        // 底部操作弹窗
        ├── UserListItem.kt                 // 单个列表项
        ├── RemarkEditDialog.kt             // 备注编辑弹窗
        └── CustomDialogs.kt                // 通用对话框
```
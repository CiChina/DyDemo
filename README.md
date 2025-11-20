# 🚀 Douyin Clone: 关注列表管理模块

本项目是一个基于现代 Android 技术栈构建的社交应用关注列表管理模块，专注于高效、响应式的用户交互体验。

## ⚙️ 技术栈 (Technical Stack)

| 类别 | 技术 | 描述 |
| :--- | :--- | :--- |
| **语言** | Kotlin | 现代、简洁的编程语言。 |
| **UI 框架** | Jetpack Compose | 声明式 UI 框架，用于构建高性能和响应式界面。 |
| **架构** | MVVM (Repository Pattern) | 遵循 Clean Architecture 原则，实现清晰的关注点分离。 |
| **数据持久化** | Room Persistence Library | 基于 SQLite 的抽象层，用于本地关注数据的高效存储。 |
| **异步处理** | Kotlin Coroutines & Flow | 用于处理数据库 I/O 操作和 UI 状态的实时更新。 |
| **依赖注入** | Hilt (Dagger) | 简化依赖关系管理。 |

## ✨ 主要功能 (Key Features)

- **关注/取关管理：** 实时处理关注和取消关注操作。
- **自定义备注名：** 支持为关注用户设置自定义备注名，并在列表和详情页优先展示。
- **动态排序：** 支持多种排序模式，例如按关注时间排序。
- **即时 UI 反馈（Pending Actions）：** 在数据库操作完成前，UI 立即响应状态变化。
- **操作限制：** 对于已取关的用户，禁用次级操作（如编辑备注），并给出友好提示。
- **信息展示优化：** 详细展示用户的原昵称、ID 和精确关注时间。

## 💡 重点技术挑战与解决方案 (Technical Deep Dive)

本项目在处理响应式 UI 和数据库延迟方面遇到了几个关键挑战，以下是解决方案的详细分析。

### 1. 数据库版本更新与 Schema 变更

#### 挑战
在修改数据库实体（如将 `followTimestamp` 字段的 `NOT NULL` 约束移除）或添加新字段时，Room 抛出 `IllegalStateException` 或 `SQLiteConstraintException`，提示 Schema 不匹配。

#### 解决方案
在项目开发阶段，我们通过增加 `AppDatabase` 的 `@Database(version = X)` 版本号，并同时在构建数据库时添加 `.fallbackToDestructiveMigration()` 来解决。这会强制 Room 在检测到不一致时，销毁旧数据库并使用新的 Schema 重建，确保开发流程的顺畅。

### 2. 关注状态的即时反馈与待处理状态管理

#### 挑战
关注/取关操作涉及数据库 I/O，存在延迟。如果 UI 必须等待 Flow 从数据库返回新数据后才更新，用户体验会很差。我们需要在不增加数据库操作负担的情况下，实现 UI 的**即时**反馈。

#### 解决方案 (ViewModel Pending Actions)
我们将“待处理状态”存储在 **ViewModel** 内部的 `pendingFollowActions: Map<Int, Boolean>` 中。

1. **用户操作后：** ViewModel 立即更新 `pendingFollowActions`，UI 响应并显示最新状态（例如，按钮变红）。
2. **异步执行：** ViewModel 在后台协程中执行数据库操作。
3. **最终一致性：** 数据库操作完成后，主数据 Flow 更新，UI 最终与数据库状态同步，ViewModel 清除 `pendingFollowActions` 中的条目。

这种方式确保了 UI 响应速度，同时将数据库交互的延迟对用户的影响降到最低。

### 3. UI 状态的准确响应与状态优先级判断

#### 挑战
由于存在 Pending Actions（待处理状态）和数据库延迟，列表项传入的 `User` 对象可能无法反映用户的最新关注状态。这导致基于旧状态的 UI 逻辑（如省略号按钮的 Toast 触发条件）判断错误。

#### 解决方案 (状态优先级机制)
我们在列表项 `UserListItem` 中实现了状态优先级判断：

1. **最高优先级：** 在判断用户状态时，首先检查 **`pendingFollowActions`** 中是否有该用户的最新状态。
2. **回退机制：** 如果 `pendingFollowActions` 中没有记录，则回退到检查主数据流中的状态，即 `user.followTimestamp != null`。

**示例逻辑：**

```kotlin
val pendingStatus = pendingFollowActions[user.id]
// 优先使用待处理状态，否则使用数据库状态
val isFollowed = pendingStatus ?: (user.followTimestamp != null)

if (isFollowed) {
    // 执行更多操作
} else {
    // 触发Toast提示：“已取关，无法使用”
}

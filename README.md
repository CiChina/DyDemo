# 🚀 Douyin Demo: 关注列表管理模块

本项目是一个基于现代 Android 技术栈构建的社交应用关注列表管理模块，专注于高效、响应式的用户交互体验。
字节跳动工程训练营-客户端-第一次作业

## ⚙️ 技术栈

| 类别 | 技术 | 备注                                   |
| :--- | :--- |:-------------------------------------|
| **语言** | Kotlin |                           |
| **UI 框架** | Jetpack Compose | 声明式 UI 框架，用于构建高性能和响应式界面。             |
| **架构** | MVVM (Repository Pattern) | 遵循 Clean Architecture 原则，实现清晰的关注点分离。 |
| **数据持久化** | Room Persistence Library | 基于 SQLite 的抽象层，用于本地关注数据的高效存储。        |
| **异步处理** | Kotlin Coroutines & Flow | 用于处理数据库 I/O 操作和 UI 状态的实时更新。          |
| **依赖注入** | Hilt (Dagger) |                             |

## ✨ 主要功能

- **关注/取关管理：** 实时处理关注和取消关注操作。
- **自定义备注名：** 支持为关注用户设置自定义备注名，并在列表和详情页优先展示。
- **动态排序：** 支持多种排序模式，例如按关注时间排序。
- **即时 UI 反馈（Pending Actions）：** 在数据库操作完成前，UI 立即响应状态变化。
- **操作限制：** 对于已取关的用户，禁用次级操作（如编辑备注），并给出友好提示。
- **信息展示优化：** 详细展示用户的原昵称、ID 和精确关注时间。

## 💡 重点技术挑战与解决方案

本项目在处理响应式 UI 和数据库延迟方面遇到了几个关键挑战，以下是解决方案的详细分析。

### 1. 数据库版本更新与 Schema 变更

#### 挑战
在修改数据库实体（如将 `followTimestamp` 字段的 `NOT NULL` 约束移除）或添加新字段时，Room 抛出 `IllegalStateException` 或 `SQLiteConstraintException`，提示 Schema 不匹配。

#### 解决方案
在项目开发阶段，我们通过增加 `AppDatabase` 的 `@Database(version = X)` 版本号，并同时在构建数据库时添加 `.fallbackToDestructiveMigration()` 来解决。这会强制 Room 在检测到不一致时，销毁旧数据库并使用新的 Schema 重建，确保开发流程的顺畅。

### 2. UI 状态的准确响应与状态优先级判断

#### 挑战
由于存在 Pending Actions（待处理状态）和数据库延迟，列表项传入的 `User` 对象可能无法反映用户的最新关注状态。这导致基于旧状态的 UI 逻辑（如省略号按钮的 Toast 触发条件）判断错误。

#### 解决方案 (状态优先级机制)
我们在列表项 `UserListItem` 中实现了状态优先级判断：

1. **最高优先级：** 在判断用户状态时，首先检查 **`pendingFollowActions`** 中是否有该用户的最新状态。
2. **回退机制：** 如果 `pendingFollowActions` 中没有记录，则回退到检查主数据流中的状态，即 `user.followTimestamp != null`。

### 3. 关注状态的即时反馈与待处理状态管理

#### 挑战
关注/取关操作涉及数据库 I/O，存在延迟。如果 UI 必须等待 Flow 从数据库返回新数据后才更新，用户体验会很差。我们需要在不增加数据库操作负担的情况下，实现 UI 的**即时**反馈。

在开发过程中，我遇到了 Compose 状态同步（State Synchronization）问题，具体如下：

1. 关注按钮按下，也就是取消关注后，刷新无法重置状态，说明UI操作和数据库更新脱节。
2. 关注列表中的“特别关注” `Switch` 在点击后会立即弹回，无法保持用户切换后的新状态。

解决该问题涉及对整个 MVVM 架构中数据流的彻底检查和优化，最终实现了 UI 层的即时响应（乐观更新）和数据层的最终一致性。

### 问题1的解决方案 (ViewModel Pending Actions)
我们将“待处理状态”存储在 **ViewModel** 内部的 `pendingFollowActions: Map<Int, Boolean>` 中。

1. **用户操作后：** ViewModel 立即更新 `pendingFollowActions`，UI 响应并显示最新状态（例如，按钮变红）。
2. **异步执行：** ViewModel 在后台协程中执行数据库操作。
3. **最终一致性：** 数据库操作完成后，主数据 Flow 更新，UI 最终与数据库状态同步，ViewModel 清除 `pendingFollowActions` 中的条目。

这种方式确保了 UI 响应速度，同时将数据库交互的延迟对用户的影响降到最低。

### 问题2的解决过程

#### 1. 数据流架构重构：引入乐观更新

**问题：** 原始设计中，`ViewModel` 调用 `Repository` 进行耗时的 `toggleSpecialFollow`（切换状态）数据库操作。由于该操作是异步的，UI 在重组时读取不到新状态，导致视觉回弹。

**修复：**

* **ViewModel (核心优化):** 弃用基于当前状态的 `toggle` 逻辑，改为接受目标状态 (`isChecked: Boolean`)。在调用 `userRepository` 进行持久化操作之前，`ViewModel` **立即**更新 `_uiState` 中对应用户的 `isSpecialFollow` 状态，触发 UI 的乐观更新。
* **Repository (效率提升):** 弃用低效的“先查询再更新” `toggleSpecialFollow` 方法，替换为高效的 `setSpecialFollow` 方法，通过 Room 的 `@Query` 直接执行 SQL `UPDATE` 设置目标状态，大大加快了持久化速度。

#### 2. UI 层状态同步：解决 Stale State 问题

**问题：** 即使 `ViewModel` 实现了乐观更新，`Switch` 依然回弹。原因在于父组件 (`FollowingScreen`) 传递了**过时的 (Stale)** `User` 对象。`FollowingScreen` 存储了 `selectedUser: User?` 的副本，当 `ViewModel` 更新全局列表时，这个副本并未更新。

**修复：**

* **Parent Composable (`FollowingScreen`):** 将存储的 `selectedUser: User?` 状态改为存储 `selectedUserId: Int?`。
* **动态查找：** 在渲染 `UserActionBottomSheet` 时，不再使用过时的副本，而是从最新的 `state.followingUsers` 列表中通过 ID 动态查找并传递**最新鲜**的 `User` 对象。

#### 3. UI 鲁棒性：构建本地和全局可跟踪状态

**问题：** 前两步并没有解决问题，为了提高 UI 响应的鲁棒性，需要显式地确保 `Switch` 的视觉状态绝对不会丢失。

**修复：**

* **SpecialFollowItem (本地状态):** 在 `SpecialFollowItem` 内部构建了一个独立的本地可观察状态 (`isSwitchChecked`)，用于控制 `Switch` 的即时视觉反馈。
* **状态同步：** 使用 `LaunchedEffect(user.isSpecialFollow)` 监听 `ViewModel` 状态，强制在 `ViewModel` 状态变化（无论是成功更新还是错误回滚）时，将本地 `isSwitchChecked` 状态与全局状态进行同步，确保了 UI 的最终正确性。

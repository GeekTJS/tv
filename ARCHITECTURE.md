# 家庭电视点播 APP 架构设计

## 技术栈
- 语言：Kotlin
- 目标平台：Android TV (API 21+)
- 目标设备：小米电视 E65X 65英寸（1920×1080px）
- IDE：Android Studio

## 核心依赖
- **播放器**：ExoPlayer（支持 M3U8 流媒体播放）
- **网络请求**：Retrofit + OkHttp + Kotlin Coroutines
- **图片加载**：Glide
- **UI组件**：AndroidX Leanback（TV专用UI库）
- **依赖注入**：Hilt
- **本地存储**：Room Database（历史记录/收藏）
- **异步处理**：Kotlin Coroutines + Flow

## 项目架构（MVVM + Clean Architecture）

```
com.familytv.app
├── data                    # 数据层
│   ├── api                 # Retrofit API接口定义
│   │   └── VodApiService.kt
│   ├── model               # 数据模型/实体
│   │   └── VodResponse.kt
│   ├── repository          # 数据仓库
│   │   └── VodRepository.kt
│   ├── local               # 本地数据库（Room）
│   │   ├── AppDatabase.kt
│   │   ├── FavoriteDao.kt
│   │   ├── HistoryDao.kt
│   │   ├── FavoriteEntity.kt
│   │   └── EntityMapper.kt
│   └── preferences         # SharedPreferences封装
├── domain                  # 业务逻辑层
│   ├── model               # 领域模型
│   ├── repository          # 仓库接口
│   └── usecase             # 用例/业务逻辑
├── presentation            # UI层
│   ├── home                # 首页相关
│   │   ├── HomeActivity.kt
│   │   ├── HomeViewModel.kt
│   │   ├── VideoListAdapter.kt
│   │   └── BannerAdapter.kt
│   ├── category            # 分类浏览
│   ├── detail              # 影片详情
│   │   ├── DetailActivity.kt
│   │   ├── DetailViewModel.kt
│   │   └── EpisodeAdapter.kt
│   ├── player              # 播放器
│   │   └── PlayerActivity.kt
│   ├── search              # 搜索
│   │   ├── SearchActivity.kt
│   │   ├── SearchViewModel.kt
│   │   └── SearchResultAdapter.kt
│   ├── favorite            # 收藏
│   └── history             # 历史记录
├── common                  # 公共模块
│   ├── utils               # 工具类
│   ├── widget              # 自定义控件
│   │   └── TvFocusHelper.kt
│   └── extension           # Kotlin扩展函数
│       ├── ViewExtensions.kt
│       └── TimeExtensions.kt
└── di                      # 依赖注入（Hilt）
    └── NetworkModule.kt
```

## 数据源接口说明

### API基础地址
`https://www.hongniuzy2.com/api.php/provide/vod/at/json/`

### 主要接口参数
- `ac=videolist` 获取视频列表
- `ac=detail` 获取详情
- `ac=list` 获取分类列表
- `t={typeid}` 按分类筛选
- `wd={keyword}` 搜索关键词
- `pg={page}` 页码

### 返回数据格式
```json
{
  "code": 1,
  "list": [
    {
      "vod_id": 12345,
      "vod_name": "影片名称",
      "vod_pic": "封面图URL",
      "vod_year": "2024",
      "vod_area": "地区",
      "vod_remarks": "更新状态",
      "vod_actor": "演员",
      "vod_director": "导演",
      "vod_content": "剧情简介",
      "vod_play_from": "播放源",
      "vod_play_url": "集数列表"
    }
  ]
}
```

## UI设计规范

### 布局规范
- 基准分辨率：1920×1080
- 边距基准：32dp（主边距）、24dp（卡片间距）
- 焦点框：白色边框2px，焦点元素放大1.05倍
- 焦点移动动画：100ms ease-out

### 色彩方案
- 主色：#FF5722（橘红色，类似南瓜电影）
- 背景色：#1A1A1A（深灰色）
- 卡片背景：#2A2A2A
- 文字颜色：#FFFFFF（主文字）、#B3B3B3（次要文字）

### 遥控器交互
- 方向键：导航移动焦点
- OK/确定：确认选择
- 返回键：返回上一级
- 播放页控制条：3秒后自动隐藏

## 核心功能实现方案

### 1. 播放器
- 使用 ExoPlayer 播放 M3U8 流
- 进度记忆存储到本地数据库
- 播放控制条自动显示/隐藏
- 左右键快进快退±10秒

### 2. 历史记录/收藏
- Room Database 本地存储
- 异步读写不影响主线程
- 支持按时间排序

### 3. 图片加载
- Glide 加载封面图
- 缓存策略：磁盘缓存+内存缓存
- 占位图和错误图处理

### 4. 焦点管理
- 自定义 FocusManager 处理焦点逻辑
- 防止焦点跳位
- 边缘越界处理

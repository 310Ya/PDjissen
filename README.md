305A-5プロジェクトデザイン実践
pushしてみた👽

前提条件
Ktlin

ディレクトリ構成
pp
└── src
└── main
├── java
│   └── com
│       └── example
│           └── pdjissen
│               └── ui
│                   ├── dashboard  (ダッシュボード画面)
│                   │   ├── DashboardFragment.kt  (← 監督！)
│                   │   ├── LocationTracker.kt    (← GPSの専門家)
│                   │   ├── MapManager.kt         (← 地図の専門家)
│                   │   └── PedometerManager.kt   (← 歩数計の専門家)
│                   │
│                   ├── friend     (フレンド画面)
│                   │   └── FriendFragment.kt
│                   │
│                   ├── home       (ホーム画面)
│                   │   └── HomeFragment.kt
│                   │
│                   ├── notifications (通知画面)
│                   │   └── NotificationsFragment.kt
│                   │
│                   ├── quest      (クエスト画面)
│                   │   └── QuestFragment.kt
│                   │
│                   └── ranking    (ランキング画面)
│                       └── RankingFragment.kt
│
├── res
│   ├── layout  (見た目)
│   │   ├── activity_main.xml
│   │   ├── fragment_dashboard.xml
│   │   ├── fragment_friend.xml
│   │   ├── fragment_home.xml
│   │   ├── fragment_notifications.xml
│   │   ├── fragment_quest.xml
│   │   └── fragment_ranking.xml
│   │
│   └── navigation  (画面の地図)
│       └── mobile_navigation.xml
│
└── manifests
└── AndroidManifest.xml
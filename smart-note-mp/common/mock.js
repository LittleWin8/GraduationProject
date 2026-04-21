export const mockUser = {
	nickname: "极客开发者",
	avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=Felix",
	bio: "沉迷代码，热爱记录。",
	stats: { notes: 12, likes: 128, collects: 45 },
	tags: ['Vue3', 'Spring Boot', '小程序', '算法']
};

export const mockNotes = [
	{ id: 1, title: "Spring Boot 性能优化指南", summary: "本文介绍了如何通过配置 JVM 参数和优化数据库连接池来提升吞吐量...", author: "架构师A", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=1", likes: 88, comments: 12, isLiked: false, isCollected: true, type: 'tech' },
	{ id: 2, title: "深漂三年的生活感悟", summary: "从南山到龙岗，记录这几年的搬家史和成长历程...", author: "小美", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=2", likes: 45, comments: 5, isLiked: true, isCollected: false, type: 'life' },
	{ id: 3, title: "如何阅读一本困难的书", summary: "读书笔记分享：面对晦涩难懂的经典著作，我们应该采取什么样的策略？", author: "书虫", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=3", likes: 120, comments: 30, isLiked: false, isCollected: false, type: 'book' },
	{ id: 4, title: "Vue3 + Vite 踩坑记录", summary: "记录在升级项目过程中遇到的插件兼容性问题及解决方案。", author: "极客开发者", avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=Felix", likes: 15, comments: 2, isLiked: false, isCollected: false, type: 'tech' }
];
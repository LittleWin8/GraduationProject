<script>
	export default {
		onLaunch: function() {
			console.log('App Launch - 智能笔记小程序已启动')
		},
		onShow: function() {
			console.log('App Show - 小程序进入前台')
		},
		onHide: function() {
			console.log('App Hide - 小程序进入后台')
		}
	}
</script>

<style lang="scss">
	/* 1. 必须在首行引入 uview-plus 的核心样式 */
	@import "./node_modules/uview-plus/index.scss";
    
	/* 2. 每个页面公共css，可以在下面写自己的全局样式 */
	.container {
		padding: 20px;
		font-size: 14px;
		line-height: 24px;
	}
</style>
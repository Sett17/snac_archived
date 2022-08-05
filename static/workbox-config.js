module.exports = {
	globDirectory: '.',
	globPatterns: [
		'**/*.{css,sass,ttf,js}'
	],
	swDest: 'sw.js',
	ignoreURLParametersMatching: [
		/^utm_/,
		/^fbclid$/
	]
};
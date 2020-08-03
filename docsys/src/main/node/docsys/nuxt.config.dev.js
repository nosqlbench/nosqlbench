import colors from 'vuetify/es5/util/colors'

var glob = require('glob');
var path = require('path');

export default {
    mode: 'spa',
    /*
    ** Headers of the page
    */
    head: {
        // titleTemplate: '%s' + " - NoSQLBench Docs",
        titleTemplate: "NoSQLBench",
        title: process.env.npm_package_name || '',
        meta: [
            {charset: 'utf-8'},
            {name: 'viewport', content: 'width=device-width, initial-scale=1'},
            {hid: 'description', name: 'description', content: process.env.npm_package_description || ''}
        ],
        link: [
            {rel: 'icon', type: 'image/x-icon', href: '/favicon.ico'}
        ]
    },
    /*
    ** Customize the progress-bar color
    */
    loading: {color: '#fff'},
    /*
    ** Global CSS
    */
    css: [],
    /*
    ** Plugins to load before mounting the App
    */
    plugins: [],
    /*
    ** Nuxt.js dev-modules
    */
    buildModules: [
        '@nuxtjs/vuetify',
    ],
    /*
    ** Nuxt.js modules
    */
    modules: [
        '@nuxtjs/axios'
    ],
    axios: {
        baseURL: "http://localhost:12345/",
    },
    /*
    ** vuetify module configuration
    ** https://github.com/nuxt-community/vuetify-module
    */
    vuetify: {
        theme: {
            dark: false,
            themes: {
                light: {
                    primary: '#51DDBD',
                    secondary: '#2D4ADE',
                    accent: '#FA7D2B',
                    // primary: '#1976D2',
                    // secondary: '#424242',
                    // accent: '#82B1FF',
                    error: '#FF5252',
                    info: '#2196F3',
                    success: '#4CAF50',
                    warning: '#FFC107'
                }
            }
        }
    },
    router: {
        mode: 'hash'
    },
    // vuetify_stock: {
    //   customVariables: ['~/assets/variables.scss'],
    //
    //   theme: {
    //     dark: true,
    //     themes: {
    //       dark: {
    //         primary: colors.blue.darken2,
    //         accent: colors.grey.darken3,
    //         secondary: colors.amber.darken3,
    //         info: colors.teal.lighten1,
    //         warning: colors.amber.base,
    //         error: colors.deepOrange.accent4,
    //         success: colors.green.accent3
    //       }
    //     }
    //   }
    // },
    /*
    ** Build configuration
    */
    build: {
        html: {
          minify: {
            collapseBooleanAttributes: false,
            decodeEntities: false,
            minifyCSS: false,
            minifyJS: false,
            processConditionalComments: false,
            removeEmptyAttributes: false,
            removeRedundantAttributes: false,
            trimCustomFragments: false,
            useShortDoctype: false
          }
        },
//        analyze: {
//            analyzerMode: 'static'
//        },
        cssSourceMap: true,
        extractCSS: false,
//        parallel: true,
        /*
        ** You can extend webpack config here
        */
        extend(config, ctx) {
            config.devtool = ctx.isClient ? 'eval-source-map' : 'inline-source-map'
            config.module.rules.push({
              test: /.g4/, loader: 'antlr4-webpack-loader'
            })
            config.module.rules.push({
              test: /\.ya?ml$/,
              use: 'js-yaml-loader',
            })
            config.node = {
                fs: 'empty'
            }
            config.optimization.minimize = false;
        }
    }
    , generate: {
        routes: dynamicRoutes
    }

}

var dynamicRoutes = getDynamicPaths({
    '/docs': 'docs/*.md',
    '/#/docs': '/#/docs/*.md'
});

function getDynamicPaths(urlFilepathTable) {
    return [].concat(
        ...Object.keys(urlFilepathTable).map(url => {
            var filepathGlob = urlFilepathTable[url];
            return glob
                .sync(filepathGlob, {cwd: 'content'})
                .map(filepath => `${url}/${path.basename(filepath, '.md')}`);
        })
    );
}

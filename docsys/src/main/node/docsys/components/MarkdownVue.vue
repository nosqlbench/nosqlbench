<template>
  <div>
    <markdown-it-vue class="md-body" :content="mdcontent" :options="mdoptions"/>
  </div>
</template>
<script>
    import MarkdownItVue from 'markdown-it-vue'
    // import 'markdown-it-vue/dist/markdown-it-vue.css'
    // var hljs = require('highlight.js');

    // let msa = require('markdown-it-smartarrows');
    // this.use(msa);

    export default {
        name: "MarkdownVue",
        components: {MarkdownItVue},
        mounted() {
            console.log("mounted");
        },

        props: {
            mdcontent: String,
            mdoptions: Object

            // mdoptions: {
            //     type: Object,
            //     default: {
            //         markdownIt: {
            //             linkify: true
            //         },
            //         linkAttributes: {
            //             attrs: {
            //                 target: '_blank',
            //                 rel: 'noopener'
            //             }
            //         }
            //     }
            // }
        }
    }
</script>
<style>

  code.hljs::before,code:before {
    content: '';
  }
  .v-application code:before,.v-application code:after {
    content: '';
  }

  code::before {
    content: '';
  }

  pre {
    padding: 5px;
    margin: 0px 0px 10px 5px;
  }
  ul {
    padding-bottom: 10px;
  }

  code:after {
    content: '';
  }
  code::after {
    content: '';
  }

  .md-body {
    padding-left: 10px;
    justify-content: left;
    align-items: left;
    text-align: left;
    vertical-align: top;
    alignment: top;
    text-align: top;
    webkit-box-align: initial;
  }

  .markdown-it-vue-alter-info {
    border: 1px solid #91d5ff;
    background-color: #e6f7ff;
  }

  .markdown-it-vue-alert-icon-info {
    color: #1890ff;
  }

  .markdown-it-vue-alter-success {
    border: 1px solid #b7eb8f;
    background-color: #f6ffed;
  }

  .markdown-it-vue-alert-icon-success {
    color: #52c41a;
  }

  .markdown-it-vue-alter-error {
    border: 1px solid #f5222d;
    background-color: #fff1f0;
  }

  .markdown-it-vue-alert-icon-error {
    color: #f5222d;
  }

  .markdown-it-vue-alter-warning {
    border: 1px solid #ffe58f;
    background-color: #fffbe6;
  }

  .markdown-it-vue-alert-icon-warning {
    color: #faad14;
  }

  .markdown-it-vue-alter {
    border-radius: 0;
    border: 0;
    margin-bottom: 0;
    display: inline-flex;
    font-family: 'Chinese Quote', -apple-system, BlinkMacSystemFont, 'Segoe UI',
    'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue',
    Helvetica, Arial, sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji',
    'Segoe UI Symbol';
    font-size: 14px;
    font-variant: tabular-nums;
    line-height: 1.5;
    color: rgba(0, 0, 0, 0.65);
    box-sizing: border-box;
    padding: 0;
    list-style: none;
    position: relative;
    padding: 8px 15px 8px 37px;
    border-radius: 4px;
    width: 100%;
    margin-bottom: 16px;
  }

  .markdown-it-vue-alter p {
    margin-bottom: 2px;
  }

  .markdown-it-vue-alert-icon {
    top: 11.5px;
    left: 16px;
    position: absolute;
  }
</style>

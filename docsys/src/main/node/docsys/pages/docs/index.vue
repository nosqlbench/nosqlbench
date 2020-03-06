<template>
  <v-app>

    <docs-menu v-model="isDrawerOpen"
               :categories="categories"
               :active_category="active_category"
               :active_topic="active_topic"/>

    <v-app-bar app dark color="secondary">
      <v-app-bar-nav-icon color="primary" @click.stop="toggleDrawer"/>
      <v-toolbar-title>nosqlbench docs</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-toolbar-items>
        <v-btn text href="https://github.com/datastax/nosqlbench-labs/wiki/Submitting-Feedback">SUBMIT FEEDBACK</v-btn>
      </v-toolbar-items>
    </v-app-bar>

    <v-content>
      <v-container>
        <v-row align="stretch">
          <div>{{testdata}}</div>

          <div class="Doc">
            <div class="doc-title">
              <h1></h1>
            </div>
            <div>
              <markdown-vue class="md-body content" :mdcontent="markdown_body"/>
            </div>
          </div>

        </v-row>
      </v-container>
    </v-content>

    <v-footer app dark color="secondary">
      <span>&copy; 2020</span>
    </v-footer>

  </v-app>
</template>
<script>
    import get_data from '~/mixins/get_data.js';
    import DocsMenu from '~/components/DocsMenu.vue'
    import MarkdownVue from "~/components/MarkdownVue";

    export default {
        mixins: [get_data],
        components: {
            DocsMenu, MarkdownVue
        },
        computed: {
            isDrawerOpen() {
                return this.$store.state.docs.isDrawerOpen;
            },
            isDrawerOpen2() {
                return this.$store.getters.drawerState;
            }
        },
        methods: {
            toggleDrawer() {
                this.$store.commit('docs/toggleDrawerState');
            }
        },
        data(context) {
            console.log("data context.params:" + JSON.stringify(context.params));
            console.log("data context.route:" + JSON.stringify(context.route));
            console.log("data context.query:" + JSON.stringify(context.query));

            return {
                testdata: this.$store.state.docs.example,
                categories_list: [],
                markdown_body: '',
                active_topic: null,
                active_category: null,
                options: function () {
                    return {
                        markdownIt: {
                            linkify: true
                        },
                        linkAttributes: {
                            attrs: {
                                target: '_blank',
                                rel: 'noopener'
                            }
                        }
                    }
                }
            }
        }
    }
</script>
<style>
  .container {
    margin: 0 auto;
    min-height: 60vh;
    display: flex;
    justify-content: center;
    align-items: center;
    text-align: center;
  }

  .title {
    font-family: 'Quicksand', 'Source Sans Pro', -apple-system, BlinkMacSystemFont,
    'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    display: block;
    font-weight: 300;
    font-size: 100px;
    color: #35495e;
    letter-spacing: 1px;
  }

  .subtitle {
    font-weight: 300;
    font-size: 42px;
    color: #526488;
    word-spacing: 5px;
    padding-bottom: 15px;
  }

  .links {
    padding-top: 15px;
  }
</style>


<template>
  <v-app>

    <docs-menu v-model="isDrawerOpen"
               :categories="categories"
               :active_category="active_category"
               :active_category_name="active_category_name"
               :active_topic="active_topic"/>

    <v-app-bar app dark color="secondary" collapse-on-scroll dense >
      <v-app-bar-nav-icon @click.stop="toggleDrawer"/>
      <v-toolbar-title>DS Bench Documentation</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-toolbar-items>
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

<template>
  <v-app>

    <docs-menu
        :active_category="active_category"
        :active_topic="active_topic"
        :categories="categories"
        @change="menuChanged()"
    @select="menuChanged()"></docs-menu>

    <v-app-bar app collapse-on-scroll dense>
      <v-app-bar-nav-icon @click.stop="toggleDrawer"/>
      <v-toolbar-title>{{ toolbarTitle }}</v-toolbar-title>
      <v-toolbar-items>
      </v-toolbar-items>

    </v-app-bar>

    <v-main>
      <markdown-vue :mdcontent="markdown_body"/>
    </v-main>

    <v-footer app>
      <span>&copy; 2020</span>
    </v-footer>

  </v-app>
</template>

<script>
import DocsMenu from '~/components/DocsMenu.vue'
import MarkdownVue from "~/components/MarkdownVue";
import docpaths from "@/js/docpaths.js"

export default {
  data() {
    return {
      markdown_body: "testing",
      active_category: null,
      active_topic: null
    }
  },
  components: {
    DocsMenu, MarkdownVue
  },
  computed: {
    categories: {
      get() {
        return this.$store.getters["docs/getCategories"]
      }
    },
    toolbarTitle: {
      get() {
        if (this.active_category) {
          return this.active_category.title
        }
        return "NoSQLBench Docs"
      }
    },
    // markdown_body: {
    //   get() {
    //     return this.$store.getters["docs/getActiveMarkdownContent"]
    //   }
    // },
    active_category: {
      get() {
        return this.$store.getters["docs/getActiveCategory"]
      },
      async set(val) {
        await this.$store.dispatch("docs/setCategories", val)
      }
    },
    active_topic: {
      get() {
        return this.$store.getters["docs/getActiveTopic"]
      },
      async set(val) {
        await this.$store.dispatch("docs/setActiveTopic")
      }
    }
  },
  async asyncData({params, route, store}) {
    await store.dispatch("docs/loadCategories")
    let categories = await store.getters["docs/getCategories"]
    let active_category =docpaths.getCategory(route,categories);
    let active_topic = docpaths.getTopic(route,categories, active_category);

    return {
      active_category,
      active_topic,
      markdown_body: active_topic.content
    }
  },
  methods: {
    async toggleDrawer() {
      await this.$store.dispatch("docs/setIsDrawerOpen", this.$store.getters["docs/getIsDrawerOpen"])
    },
    menuChanged(evt) {
      console.log("menu changed:" + JSON.stringify(evt, null, 2))
      this.$forceUpdate()
    }
  }
}
</script>
<style>
.container {
  min-height: 60vh;
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  text-align: start;
  margin: 0 auto 0 15px;
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


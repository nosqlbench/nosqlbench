<template>
  <v-navigation-drawer app
                       v-model="isDrawerOpen"
                       :permanent="isMenuLocked"
                       @transitionend="afterMenuTransition"
                       :title="drawerTitle">

    <div class="menu" >

      <!--      active_category: {{ active_category.name }} active_topic: {{ active_topic.name }}-->
      <!-- Use active_category and active_topic to select inactive -->

      <v-list nav dense>

        <!-- KEEP OPEN -->
        <v-list-item>
          <!--          <v-list-item v-if="$vuetify.breakpoint.mdAndDown">-->
          <v-list-item-action>
            <v-switch inset v-model="isMenuLocked" label="keep open"
                      @click="toggleMenuLocked"></v-switch>
          </v-list-item-action>
        </v-list-item>

        <!--              link :to="category.path">{{ category.title }}-->
        <!-- by category -->
        <v-list-group v-for="(category,c) in categories" :key="c"
                      :value="active_category.title === category.title" active-class="isactive">
          <template v-slot:activator>
            <v-list-item-content @click="$nuxt.$router.push({path: category.path})">
              <v-list-item-title>{{ category.title }}</v-list-item-title>
            </v-list-item-content>
          </template>

          <!-- by topic -->
          <v-list-item v-for="(topic, i) in category.topics" :key="i" link :to="topic.path">
            <v-list-item-title>{{ topic.title }}</v-list-item-title>
          </v-list-item>

        </v-list-group>
      </v-list>

    </div>
  </v-navigation-drawer>
</template>
<script>
export default {
  name: 'DocsMenu',
  props: {
    categories: Array,
    active_category: Object,
    active_topic: Object
  },
  data() {
    let drawer = null;
    return {
      drawer
    }
  },
  computed: {
    drawerTitle: {
      get() {
        return "category=" + this.active_category.name
            + "\ntopic=" + this.active_topic.name
      }
    },
    isMenuLocked: {
      get() {
        return this.$store.getters["docs/getIsMenuLocked"]
      }
    },
    isDrawerOpen: {
      get() {
        return this.$store.getters["docs/getIsDrawerOpen"]
      },
      set(val) {
        this.$store.dispatch("docs/setIsDrawerOpen", val)
      }
    }
  },
  methods: {
    clickCategory(category) {
      this.$store.dispatch("docs/setActiveCategory", category)
      this.$nuxt.$router.push({path: category.path})
    },
    clickTopic(category, topic) {
      this.$store.dispatch("docs/setActiveCategory", category)
      this.$store.dispatch("docs/setActiveTopic", topic)
      this.$nuxt.$router.push({path: topic.path})
    },
    afterMenuTransition() {
      this.drawer = !this.drawer;
      // console.log("drawer now " + (this.drawer ? "true" : "false"))
    },
    toggleMenuLocked() {
      this.$store.dispatch("docs/setIsMenuLocked", !this.$store.getters["docs/getIsMenuLocked"])

    }
  },
  async created() {
    await this.$store.dispatch("docs/loadCategories");
  }
}
</script>
<style>

/*.v-list-item {*/
/*        color: #FFFFFF;*/
/*}*/

/*.v-list-item--title {*/
/*        color: #FFFFFF;*/
/*}*/

.v-list-item--active {
  /*border: 1px black;*/
  background-color: #EEEEEE;
  /*color: #FFFFFF !important;*/
}

/*.v-list-item--disabled {*/
/*    color: #DDDDDD !important;*/
/*}*/

/*div.theme--light.v-list-item:not(.v-list-item--active):not(.v-list-item--disabled) {*/
/*    color: #DDDDDD !important;*/
/*}*/
/*a.theme--light.v-list-item:not(.v-list-item--active):not(.v-list-item--disabled) {*/
/*    color: #DDDDDD !important;*/
/*}*/

/*.nuxt-link-exact-active {*/
/*    !*color: #52c41a;*!*/
/*    background-color: #7F828B;*/
/*    color: #52c41a;*/
/*    !*color: #000000;*!*/
/*}*/

/*.isactive {*/
/*    background-color: #7F828B;*/
/*    color: #52c41a;*/
/*}*/

/*.router-link-active {*/
/*    background-color: #FFFFFF;*/
/*    color: #FFFFFF;*/

/*}*/
</style>

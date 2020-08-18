<template>
  <v-container>
    <v-menu offset-y>
      <template v-slot:activator="{ on, attrs }">
        <v-btn title="App Selector" v-bind="attrs" v-on="on">APPS</v-btn>
      </template>
      <v-list>
        <v-btn to="/ui/run" title="Run a workload">Run</v-btn>
        <v-btn to="/ui/build" title="Build a workload from a schema" >Build</v-btn>
        <v-btn to="/ui/status" title="See running workloads" >Status</v-btn>
        <v-btn to="/ui/workspaces" title="Run a workload">Workspaces</v-btn>
      </v-list>
    </v-menu>
<!--    <v-select dense-->
<!--              hide-details="auto"-->
<!--              label="app"-->
<!--              v-model="thisapp"-->
<!--              :items="empty"-->
<!--              @select="selected(thisapp)"-->
<!--              @change="selected(thisapp)"-->
<!--    >-->
<!--      <template v-slot:append-item>-->
<!--        <v-list-item><nuxt-link to="/ui/run">Run</nuxt-link></v-list-item>-->
<!--        <v-list-item><nuxt-link to="/ui/build">Build</nuxt-link></v-list-item>-->
<!--        <v-list-item><nuxt-link to="/ui/status">Status</nuxt-link></v-list-item>-->
<!--        <v-list-item><nuxt-link to="/ui/workspaces">Workspaces</nuxt-link></v-list-item>-->
<!--      </template>-->
<!--    </v-select>-->
  </v-container>
</template>

<script>
export default {
  name: 'app-selector',
  data() {
    let data = {
      empty: [],
      apps: ['build','run','status','workspaces'],
      thisapp: 'build'
    };
    return data;
  },
  computed: {
    current: {
      get: function() {}
    }
  },
  methods: {
    selected(evt) {
      console.log("selected:" + JSON.stringify(evt))
      let selected_path = "/ui/" + evt;
      if (!this.$route.fullPath.includes(selected_path)) {
        // this.$router.
      }
    }
  }
}
</script>

<style>
</style>

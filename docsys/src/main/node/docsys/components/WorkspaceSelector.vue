<template>
  <v-container fluid>
    <v-select :items="workspaces" label="default workspace"></v-select>
  </v-container>
</template>
<script>

export default {
  name: 'WorkspaceSelector',
  data(context) {
    let data = {
      workspaces: [],
      workspace: 'default',
      enabled: false
    };
    return data;
  },
  async asyncData({$axios, store}) {
    let enabled = await $axios.$get("/services/nb/enabled")
        .then(res => {
          return res
        })
        .catch((e) => {
          console.log("back-end not found");
        })
    let workspaces = await $axios.$get("/services/workspaces/")
        .then(res => {
          return res
        })
        .catch((e) => {
          console.log("back-end not found");
        })
    return {enabled, workspaces}
  }
}
</script>
<style>
</style>

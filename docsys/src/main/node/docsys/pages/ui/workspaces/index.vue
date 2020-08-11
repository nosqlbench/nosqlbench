<template>
  <v-app>
    <v-app-bar app dark color="secondary">
      <v-toolbar-title>NoSQLBench - Workspaces</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-toolbar-items>
        <v-btn text href="https://github.com/nosqlbench/nosqlbench/wiki/Submitting-Feedback">SUBMIT FEEDBACK</v-btn>
      </v-toolbar-items>
    </v-app-bar>

    <v-layout justify-center align-center>
      <v-content>
        <v-card max-width="344">
          <v-card-title>workspace-name</v-card-title>
          <v-card-text>workspace details go here</v-card-text>
        </v-card>

      </v-content>
    </v-layout>

  </v-app>
</template>

<script>
export default {
name: "workspaces.vue"
}
</script>

<style scoped>

</style>

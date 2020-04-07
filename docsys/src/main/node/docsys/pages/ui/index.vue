<template>
  <v-app>

    <v-app-bar app dark color="secondary">
      <v-toolbar-title>NoSQLBench</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-toolbar-items>
        <v-btn text href="https://github.com/nosqlbench/nosqlbench/wiki/Submitting-Feedback">SUBMIT FEEDBACK</v-btn>
      </v-toolbar-items>
    </v-app-bar>

    <v-content>
      <v-container v-if="enabled">
        <v-row align="stretch">


          <v-col class="d-flex" cols="12" sm="6">
            <v-select
              :items="workloads"
              chips
              label="Workload"
            ></v-select>
          </v-col>


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

    export default {
        mixins: [get_data],
        components: {
        },
        computed: {
        },
        methods: {
        },
        data(context) {
            let data = {
                workloads: [1, 2, 3],
                enabled: false
            };
            return data;
        },
        async asyncData({ $axios, store }) {
          let enabled = await $axios.$get("/services/nb/enabled")
                    .then(res => {
                        return res
                    })
                    .catch((e) => {
                        console.log("back-end not found");
                    })
          let workloads = await $axios.$get("/services/nb/workloads")
                    .then(res => {
                        return res
                    })
                    .catch((e) => {
                        console.log("back-end not found");
                    })


          return {
              enabled: enabled,
              workloads: workloads,
          }
        },
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

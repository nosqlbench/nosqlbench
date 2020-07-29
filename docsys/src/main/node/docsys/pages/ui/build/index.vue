<template>
  <v-app>

    <v-app-bar app dark color="secondary">
      <v-toolbar-title>NoSQLBench - Worlkoad Generator</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-toolbar-items>
        <v-btn text href="https://github.com/nosqlbench/nosqlbench/wiki/Submitting-Feedback">SUBMIT FEEDBACK</v-btn>
      </v-toolbar-items>
    </v-app-bar>

    <v-layout
      justify-center
      align-center>

    <v-main>
        <v-container fluid>
            <v-layout row>
                <v-flex>
                    <v-card>
                        <v-card-title>
                            Workload details
                        </v-card-title>
                        <v-col
                                cols="12"
                                sm="6"
                                md="10"
                                lg="10"
                        >
                            <v-textarea
                                    outlined
                                    label="Create Table Statement"
                                    v-model="createTableDef"
                                    v-on:blur="parseStatement()"
                            ></v-textarea>

                        </v-col>

                        <v-col cols="12">
                        </v-col>
                    </v-card>
                </v-flex>

            </v-layout>


        </v-container>

    </v-main>
    </v-layout>

      <v-footer app dark color="secondary">
          <span>&copy; 2020</span>
      </v-footer>

  </v-app>
</template>
<script>
    import get_data from '~/mixins/get_data.js';
    import CqlParser from '~/antlr/CqlParser.js';

    export default {
        mixins: [get_data],
        components: {
        },
        computed: {
        },
        methods: {
            async parseStatement() {
                console.log(this.$data.createTableDef);
                console.log(CqlParser)
                debugger
                /*
                const data = await this.$axios.$get('/services/nb/parameters?workloadName=' + this.workloadName)
                if (!data.err) {
                    this.$data.templates = data;
                }
                 */
            },
        },
        data(context) {
            let data = {
                enabled: false,
                createTableDef: "",
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
            return {
                enabled: enabled,
            }
        },
    }
</script>
<style>
    .container {
        margin: 0 auto;
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

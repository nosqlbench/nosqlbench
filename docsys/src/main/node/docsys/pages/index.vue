<template>
  <!-- https://github.com/Microsoft/vscode-recipes/tree/master/vuejs-cli -->
    <div class="container" style="justify-content: center; align-content: center; display: flex; width: 100%; margin-left: unset">
<!--        <recursive-menu/>-->
        <div style="justify-content: center" align="middle">
            <logo align="middle" style="justify-content: center; align-content: center"></logo>
            <h1 class="title">nosqlbench</h1>
            <h2 class="subtitle">open source, pluggable, nosql benchmarking suite</h2>
            <div class="links">
                <nuxt-link to="/docs/" class="button--purple">Documentation</nuxt-link>
                <a href="https://github.com/nosqlbench/nosqlbench" class="button--purple">Project Site</a>
            </div>
        </div>
    </div>
</template>

<script>
    import Logo from '~/components/Logo.vue'

    export default {
        components: {
            Logo
        }
    }
</script>

<style>
    /*.container {*/
    /*    margin: 0 auto;*/
    /*    min-height: 60vh;*/
    /*    display: flex;*/
    /*    justify-content: center;*/
    /*    align-items: center;*/
    /*    text-align: center;*/
    /*}*/

    /*.title {*/
    /*    font-family: 'Quicksand', 'Source Sans Pro', -apple-system, BlinkMacSystemFont,*/
    /*    'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;*/
    /*    display: block;*/
    /*    font-weight: 300;*/
    /*    font-size: 100px;*/
    /*    color: #35495e;*/
    /*    letter-spacing: 1px;*/
    /*}*/

    /*.subtitle {*/
    /*    font-weight: 300;*/
    /*    font-size: 42px;*/
    /*    color: #526488;*/
    /*    word-spacing: 5px;*/
    /*    padding-bottom: 15px;*/
    /*}*/

    /*.links {*/
    /*    padding-top: 15px;*/
    /*}*/
</style>

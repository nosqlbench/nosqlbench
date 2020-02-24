
// https://www.mikestreety.co.uk/blog/vue-js-using-localstorage-with-the-vuex-store
export const state = () => ({
    isDrawerOpen: true,
    isDrawerPinned: false
});

export const mutations = {
    // initializeStore(state) {
    //     if(localStorage.getItem('store')) {
    //         this.replaceState(
    //             Object.assign(state,JSON.parse(localStorage.getItem('store')))
    //         );
    //     }
    // },
    toggleDrawerState(state, newDrawerState) {
        if (state.isDrawerPinned) {
            return;
        }
        state.isDrawerOpen=!state.isDrawerOpen;
    },
    setDrawer(state, newDrawerState) {
        if (state.isDrawerPinned) {
            return;
        }
        state.isDrawerOpen=newDrawerState;
    },
    setMenuLock(state, newLockState) {
        state.isDrawerPinned=newLockState;
    }
};
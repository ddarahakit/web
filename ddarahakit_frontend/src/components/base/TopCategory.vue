<script setup>
import { computed } from 'vue'
import { useRoute } from "vue-router";
import useCategoryStore from '@/stores/useCategoryStore'

//카테고리 저장소 정보 객체
const categoryStore = useCategoryStore();

const route = useRoute();


</script>

<template>
    <div class="home">
            <div class="breadcrumbs_container">
                <div class="container">
                    <div class="row">
                        <div class="col">
                            <div class="breadcrumbs">
                                <ul v-if="route.meta.category">
                                    <li><a href="/">Home</a></li>
                                    <li v-for="category in route.meta.category">
                                        <RouterLink :to="category.path">{{ category.name }}</RouterLink>
                                    </li>
                                </ul>
                                <ul v-else>
                                    <li><a href="/">Home</a></li>
                                    <li v-for="category in categoryStore.category">
                                        <RouterLink :to="`/course/list/${category.slug}`">{{ category.name }}</RouterLink>
                                    </li>

                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
</template>

<style scoped>

/*********************************
6. Home
*********************************/

.home {
    width: 100%;
    height: 6.5rem;
    background: #f2f4f5;
    border-bottom: solid 1px #edeff0;
}

.breadcrumbs_container {
    position: absolute;
    left: 0;
    bottom: 0;
    width: 100%;
    padding-bottom: 13px;
    padding-left: 3px;
}

.breadcrumbs ul li {
    display: inline-block;
    position: relative;
}

.breadcrumbs ul li:not(:last-child)::after {
    display: inline-block;
    font-family: 'FontAwesome';
    content: '\f105';
    margin-left: 0.5rem;
    margin-right: 0.5rem;
    color: #384158;
}

.breadcrumbs ul li a {
    font-size: 14px;
    font-weight: 400;
    color: #384158;
    -webkit-transition: all 200ms ease;
    -moz-transition: all 200ms ease;
    -ms-transition: all 200ms ease;
    -o-transition: all 200ms ease;
    transition: all 200ms ease;
}

.breadcrumbs ul li a:hover {
    color: #14bdee;
}

</style>
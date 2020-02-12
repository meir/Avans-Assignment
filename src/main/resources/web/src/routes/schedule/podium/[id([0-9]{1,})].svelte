<script context="module">
export async function preload(page, session) {
    return page.params
}
</script>

<script>

import { onMount } from 'svelte'

import Navbar from '../../../components/navbar.svelte'
import Schedule from '../../../components/schedule.svelte'

export let id;
let data = []

onMount(async () => {
	fetch("http://localhost:8080/api/performances/get").then(resp => {
        return resp.json()
    }).then(resp => {
        data = resp.filter(e => {
			return e.podium.id == id
		})
    })
})

</script>

<div>
	<Navbar/>
	<Schedule data={data}/>
</div>

<style>

</style>
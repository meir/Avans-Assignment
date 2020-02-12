<script>

import { onMount } from 'svelte'
import ArtistBlock from './artistblock.svelte'

export let data = [];

onMount(async () => {
    if(process.browser) {
        if(!(data.length > 0)) {
            fetch("http://localhost:8080/api/artists/get").then(resp => {
                return resp.json()
            }).then(resp => {
                data = resp
                console.log(data)
            })
        }
    }
})

</script>

<div id="artistslist">
    {#each data as artist}
        <div>
            <ArtistBlock data={artist}/>
        </div>
    {/each}
</div>

<style>

div#artistslist {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    border: 1px solid rgba(0, 0, 0, .2);
}

</style>
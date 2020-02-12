<script>

import Modal from './modal.svelte'

let links = [
    {
        text: "Artists/Bands",
        url: "/"
    },
    {
        text: "Schedule",
        url: "/schedule"
    },
    {
        text: "Podia",
        url: "/podia"
    }
]
</script>

<nav>
    <ul>
        {#each links as link}
            <li>
                <a class:active="{process.browser ? document.location.href.endsWith(link.url) : false}" href={ link.url }>{ link.text }</a>
            </li>
        {/each}
    </ul>
    <Modal/>
</nav>

<style>
nav {
    padding: 1rem;
    display: flex;
}

ul {
    display: flex;
    list-style-type: none;
}

a {
    margin: .2rem;
    padding: 1rem;
    border-radius: 5px;
    border: 1px solid rgba(0, 0, 0, .1);
    transition: border .2s linear;
    text-decoration: none;
}

a:hover {
    border: 1px solid rgba(0, 0, 0, .3);
}

.active {
    border: 1px solid rgba(0, 0, 0, .3) !important;
}
</style>
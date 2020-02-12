<script>
  export let data;
</script>

<style>
  #bottom {
    margin-top: auto;
  }

  div#block {
    display: inline-grid;
    width: 175px;
    height: 200px;
    margin: 0.4rem;
    border: 1px solid rgba(0, 0, 0, 0.2);
    padding: 0.5rem;
    border-radius: 5px;
  }

  h4 {
    padding: 0;
    margin: 0;
    letter-spacing: 1.2pt;
    text-transform: uppercase;
  }

  #operations {
    cursor: pointer;
  }
</style>

<div id="block">
  <h4>{data.name}</h4>
  <div>{data.description}</div>
  <div id="bottom">
    <div>{data.type}</div>
    <a href={'/schedule/artist/' + data.id}>See performances ></a>
    <div id="operations" on:click={() => {
            if(process.browser) window.openModal("Edit", "http://localhost:8080/api/artist/update", data)  
        }}>
            Edit
    </div>
    <div
        id="operations"
        on:click={() => {
            fetch('http://localhost:8080/api/artist/delete', {
                method: 'POST',
                body: JSON.stringify({
                    id: Number(data.id)
                })
            }).then(resp => {
                return resp.json();
            }).then(resp => {
                if (resp.success) {
                    if (process.browser) location.reload();
                }
            });
        }}>
        Delete
    </div>
  </div>
</div>
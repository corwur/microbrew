import {Subject} from "rxjs";
import {filter, map} from "rxjs/operators";

export class SelectGeneForm {

    constructor() {
        this.subject = new Subject()
    }

    renderForm() {
        return `<form id="selectGeneForm" class="form-inline my-2 my-lg-0" autocomplete="off">
            <div>
            <input class="form-control" id="selectGeneId" name="Gene identifier" placeholder="Gene identifier">
            <div id="gene-id-autocomplete"></div>
            </div>
            <input class="form-control" value="1" style="max-width: 80pt" id="distance" name="Distance"
        type="number" placeholder="Distance">
            <input type="submit" value="Show">
            </form>`
    }

    renderList(genes) {
        if(genes && genes.length > 0) {
            const items = genes.map(geneId => `<li><a href="#">` + geneId + `</a></li>`).reduce((a,b) => a + b)
            return `<ul class="select-gene">` + items + `</ul>`
        } else {
            return `<ul class="select-gene"></ul>`
        }
    }

    showGeneIdList(data) {
        document.getElementById("gene-id-autocomplete").innerHTML = this.renderList(data.genes)
        const clickGene = function (geneId, event) {
            event.returnValue = false;
            document.getElementById("selectGeneId").value = geneId
            document.getElementById("gene-id-autocomplete").innerHTML = ""
            return false
        }
        document.querySelectorAll("#gene-id-autocomplete a")
            .forEach(el => el.addEventListener('click', event => clickGene(el.textContent, event)))
    }

    selectGene(elem, event) {
        event.returnValue = false;
        this.geneId = elem.elements[0].value
        this.distance = elem.elements[1].value ? elem.elements[1].value : 1;
        this.subject.next({selectGeneId:{geneId: this.geneId, distance:this.distance}})
        // this.service.getStructure(this.geneId, this.distance).subscribe(data => this.model.next({structure: data, geneId:this.geneId}))
        return false
    }

    findGene(geneId, key) {
        console.log("findGene " + JSON.stringify(geneId) + JSON.stringify(key)  )
        if (geneId && geneId.length >= 1 && key != "Shift") {
            this.subject.next({'findGeneId': geneId})
        }
    }

    getSelectedGeneIdObservable() {
        return this.bind('selectGeneId')
    }
    getFindGeneIdObservable(geneId) {
        return this.bind('findGeneId')
    }

    getExpandNodeMenu() {
        return this.bind('expandNodeMenu')
    }

    getExpandNodeQuery(event) {
        return this.bind('expandNodeQuery')
    }

    getExpandNodeOnQuery (event, type, directions){
        return this.bind('expandNodeOnQuery')
    }


    bind(eventName) {
        return this.subject.asObservable()
            .pipe(filter( data => data.hasOwnProperty(eventName)))
            .pipe(map(data => {
                console.log("select gene form bind " + eventName + " " + JSON.stringify(data))
                return data[eventName]
            }))
    }

    init(config) {
        const _this = this
        document.getElementsByTagName('gene-structure-form')[0].innerHTML = this.renderForm()
        document.querySelector("#selectGeneId").addEventListener('keyup', event => _this.findGene(event.target.value, event.key))
        document.querySelector("#selectGeneForm").addEventListener('submit', event => _this.selectGene(event.target, event))
  }
}

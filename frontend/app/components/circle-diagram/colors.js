export class Colors  {
	constructor() {
		this.colors = ["#A4262C", "#8F7034","#407855","#038387","#CA5010","#0078D4","#40587C","#4052AB","#854085","#8764B8","#737373","#867365"];
		this.currentColor = 0;
	}
	pick(){
		var col = this.colors[this.currentColor];
		this.currentColor++;
		if (this.currentColor >= this.colors.length) {
			this.currentColor = 0;
		}
		return col;
	}
}
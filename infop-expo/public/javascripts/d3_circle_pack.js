var InfopExpo = InfopExpo || {};

// Circle packing - accepts an optional override object
InfopExpo.D3CirclePack = function() {

    var options = arguments.length > 0 ? arguments[0] : {};

    this.margin = options.margin || 20;
    this.container = options.container || "#viz",
    this.uniqueTermsList = options.uniqueTermsList || "#topTerms--unique";
    this.commonTermsList = options.commonTermsList || "#topTerms--common";
    this.diameter = $(this.container).width();

    return this;
}

InfopExpo.D3CirclePack.prototype.draw = function(dataPath) {

    var margin = this.margin,
        container = this.container,
        uniqueTermsList = this.uniqueTermsList,
        commonTermsList = this.commonTermsList,
        diameter = this.diameter;

    var color = d3.scale.linear()
        .domain([-1, 5])
        .range(["hsl(152,80%,80%)", "hsl(228,30%,40%)"])
        .interpolate(d3.interpolateHcl);

    var pack = d3.layout.pack()
        .padding(2)
        .size([diameter - margin, diameter - margin])
        .value(function(d) { return d.size; });

    var svg = d3.select(container).append("svg")
        .attr("width", diameter)
        .attr("height", diameter)
        .attr("xmlns", "http://www.w3.org/2000/svg")
        .attr("xmlns:xlink", "http://www.w3.org/1999/xlink")
      .append("g")
        .attr("transform", "translate(" + diameter / 2 + "," + diameter / 2 + ")");

    d3.json(dataPath, function(error, root) {
      if (error) throw error;

      var focus = root,
          nodes = pack.nodes(root),
          view;

      var circle = svg.selectAll("circle")
          .data(nodes)
        .enter().append("circle")
          .attr("class", function(d) { return d.parent ? d.children ? "node" : "node node--leaf" : "node node--root"; })
          .style("fill", function(d) { return d.children ? color(d.depth) : null; })
          .on("click", function(d, i) {
            if (focus === d) {
              if (!d.children) {
                window.open(d.name, "_blank");
                d3.event.stopPropagation();
              }
              return;
            }

            zoom(d), d3.event.stopPropagation();
          });

      var labels = svg.selectAll("text")
          .data(nodes)
          .enter().append("text")
            .attr("class", "label")
            .style("fill-opacity", function(d) { return d.parent === root ? 1 : 0; })
            .style("display", function(d) { return d.parent === root ? "inline" : "none"; })
            .text(function(d) { return d.name; });

      var uniqueTerms = d3.select(uniqueTermsList)
                          .selectAll("li")
                          .data(focus.uniqueTerms)
                          .enter().append("li")
                            .attr("class", "list-group-item")
                            .text(function(d) { return d.term; });

      var commonTerms = d3.select(commonTermsList)
                          .selectAll("li")
                          .data(focus.commonTerms)
                          .enter().append("li")
                            .attr("class", "list-group-item")
                            .text(function(d) { return d.term; });

      var node = svg.selectAll("circle,text");

      d3.select(container)
          .style("background", color(-1))
          .on("click", function() { zoom(root); });

      zoomTo([root.x, root.y, root.r * 2 + margin]);

      function zoom(d) {
        var focus0 = focus; focus = d;
        var transition = d3.transition()
            .duration(d3.event.altKey ? 7500 : 750)
            .tween("zoom", function(d) {
              var i = d3.interpolateZoom(view, [focus.x, focus.y, focus.r * 2 + margin]);
              return function(t) { zoomTo(i(t)); };
            });

        transition.selectAll("text")
          .filter(function(d) { return (d === focus && !d.children) || d.parent === focus || this.style.display === "inline"; })
            .style("fill-opacity", function(d) { return (d === focus && !d.children) || d.parent === focus ? 1 : 0; })
            .each("start", function(d) { if ((d === focus && !d.children) || d.parent === focus) this.style.display = "inline"; })
            .each("end", function(d) { if ((d !== focus || d.children) && d.parent !== focus) this.style.display = "none"; });

        console.log(focus.uniqueTerms.map(function(e) { return e.term; }));
        $(uniqueTermsList + " li").remove();
        d3.select(uniqueTermsList)
          .selectAll("li")
          .data(focus.uniqueTerms)
          .enter().append("li")
            .attr("class", "list-group-item")
            .text(function(d) { return d.term; });

        console.log(focus.commonTerms.map(function(e) { return e.term; }));
        $(commonTermsList + " li").remove();
        d3.select(commonTermsList)
          .selectAll("li")
          .data(focus.commonTerms)
          .enter().append("li")
            .attr("class", "list-group-item")
            .text(function(d) { return d.term; });

      }

      function zoomTo(v) {
        var k = diameter / v[2]; view = v;
        node.attr("transform", function(d) { return "translate(" + (d.x - v[0]) * k + "," + (d.y - v[1]) * k + ")"; });
        circle.attr("r", function(d) { return d.r * k; });
      }
    });

    d3.select(self.frameElement).style("height", diameter + "px");

};

InfopExpo.D3CirclePack.prototype.fadeOut = function() {
    d3.select("svg").remove();
};
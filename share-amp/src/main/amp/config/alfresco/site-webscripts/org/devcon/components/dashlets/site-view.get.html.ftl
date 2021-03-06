<!-- Author : Bhagya Silva @bhagyas -->
<script type="text/javascript">//<![CDATA[
   new DevCon.dashlet.SiteView("${args.htmlid}").setOptions(
   {
      "key": "value",
      "height" : "${args.height}"
   }).setMessages(${messages});
   new Alfresco.widget.DashletResizer("${args.htmlid}", "${instance.object.id}");
//]]></script>
<script type="text/javascript">//<![CDATA[
   new Alfresco.widget.DashletResizer("${args.htmlid}", "${instance.object.id}");
//]]></script>
<div class="dashlet">
   <div class="title">${msg("header")}</div>
   <div class="body scrollableList"<#if args.height??> style="height: ${args.height}px;"</#if>>
	<style type="text/css">
		text {
		  font-size: 11px;
		  pointer-events: none;
		}
		
		text.parent {
		  fill: #1f77b4;
		}
		
		circle {
		  fill: #ccc;
		  stroke: #999;
		  pointer-events: all;
		}
		
		circle.parent {
		  fill: #1f77b4;
		  fill-opacity: .1;
		  stroke: steelblue;
		}
		
		circle.parent:hover {
		  stroke: #ff7f0e;
		  stroke-width: .5px;
		}
		
		circle.child {
		  pointer-events: none;
		}
    </style>

	
	<div class="d3_content"></div>
     <!-- D3 Visualization goes here -->
   </div>
   
   <script type="text/javascript">
		var siteShortName = "${siteShortName}";
		var dashletHeight = "<#if args.height??>${args.height}<#else>600</#if>";
		
		var w = 800,
			h = dashletHeight,
			r = dashletHeight * 0.75,
			x = d3.scale.linear().range([0, r]),
			y = d3.scale.linear().range([0, r]),
			node,
			root;
		
		var pack = d3.layout.pack()
			.size([r, r])
			.value(function(d) { return d.size; })
		
		var vis = d3.select("div.d3_content").insert("svg:svg", "h2")
			.attr("width", w)
			.attr("height", h)
		  .append("svg:g")
			.attr("transform", "translate(" + (w - r) / 2 + "," + (h - r) / 2 + ")");
		
		var jsonTreeUrl =  Alfresco.constants.URL_PAGECONTEXT + "components/avt-data/birdview?site=" + siteShortName;
		
		d3.json(jsonTreeUrl, function(data) {
		  node = root = data;
		
		  var nodes = pack.nodes(root);
		
		  vis.selectAll("circle")
			  .data(nodes)
			.enter().append("svg:circle")
			  .attr("class", function(d) { return d.children ? "parent" : "child"; })
			  .attr("cx", function(d) { return d.x; })
			  .attr("cy", function(d) { return d.y; })
			  .attr("r", function(d) { return d.r; })
			  .on("click", function(d) { return zoom(node == d ? root : d); });
		
		  vis.selectAll("text")
			  .data(nodes)
			.enter().append("svg:text")
			  .attr("class", function(d) { return d.children ? "parent" : "child"; })
			  .attr("x", function(d) { return d.x; })
			  .attr("y", function(d) { return d.y; })
			  .attr("dy", ".35em")
			  .attr("text-anchor", "middle")
			  .style("opacity", function(d) { return d.r > 20 ? 1 : 0; })
			  .text(function(d) { return d.name; });
		
		  d3.select(window).on("click", function() { zoom(root); });
		});
		
		function zoom(d, i) {
		  var k = r / d.r / 2;
		  x.domain([d.x - d.r, d.x + d.r]);
		  y.domain([d.y - d.r, d.y + d.r]);
		
		  var t = vis.transition()
			  .duration(d3.event.altKey ? 7500 : 750);
		
		  t.selectAll("circle")
			  .attr("cx", function(d) { return x(d.x); })
			  .attr("cy", function(d) { return y(d.y); })
			  .attr("r", function(d) { return k * d.r; });
		
		  t.selectAll("text")
			  .attr("x", function(d) { return x(d.x); })
			  .attr("y", function(d) { return y(d.y); })
			  .style("opacity", function(d) { return k * d.r > 20 ? 1 : 0; });
		
		  node = d;
		  d3.event.stopPropagation();
		}

	</script>
</div>
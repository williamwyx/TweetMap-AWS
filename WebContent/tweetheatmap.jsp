<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Get Locations of Recent Tweets!</title>
<style>
html, body, #map-canvas {
	height: 100%;
	margin: 0px;
	padding: 0px
}

#panel {
	position: absolute;
	top: 5px;
	left: 50%;
	margin-left: -180px;
	z-index: 5;
	background-color: #fff;
	padding: 5px;
	border: 1px solid #999;
}
</style>
<script
	src="https://maps.googleapis.com/maps/api/js?v=3.exp&signed_in=true&libraries=visualization"></script>
<script>
	var map;
	var pointArray, pa_sport, pa_music, pa_tech, pa_magic;
	var heatmap, hm_sport, hm_music, hm_tech, hm_maigc;
	var tweetData = [];
	var sportData = [];
	var musicData = [];
	var techData = [];
	var magicData = [];
	var wsUri = "ws://localhost:8080/TweetMap/dataserver";

	/* Initializaion */
	function initialize() {
		// init the map
		var mapOptions = {
			zoom : 2,
			center : new google.maps.LatLng(31.719597, 12.420473),
			mapTypeId : google.maps.MapTypeId.SATELLITE
			//mapTypeId : google.maps.MapTypeId.ROADMAP
		};
		map = new google.maps.Map(document.getElementById('map-canvas'),
				mapOptions);

		pointArray = new google.maps.MVCArray(tweetData);
		heatmap = new google.maps.visualization.HeatmapLayer({
			data : pointArray
		});
		//heatmap.setMap(map);

		pa_sport = new google.maps.MVCArray(sportData);
		pa_music = new google.maps.MVCArray(musicData);
		pa_tech = new google.maps.MVCArray(techData);
		pa_magic = new google.maps.MVCArray(magicData);
		hm_sport = new google.maps.visualization.HeatmapLayer({
			data : pa_sport
		});
		hm_music = new google.maps.visualization.HeatmapLayer({
			data : pa_music
		});
		hm_tech = new google.maps.visualization.HeatmapLayer({
			data : pa_tech
		});
		hm_magic = new google.maps.visualization.HeatmapLayer({
			data : pa_magic
		});
		hm_sport.setMap(map);
		hm_music.setMap(map);
		hm_tech.setMap(map);
		hm_magic.setMap(map);

		// init websocket connection
		websocket = new WebSocket(wsUri);
		websocket.onopen = function(evt) {
			onOpen(evt)
		};
		websocket.onmessage = function(evt) {
			onMessage(evt)
		};
		websocket.onerror = function(evt) {
			onError(evt)
		};

		// init the test output
		output = document.getElementById("output");
	}

	/* Functions for map manipulation */
	function toggleHeatmap() {
		var select = document.getElementById("topicList");
		var topic = select.options[select.selectedIndex].value;
		switch (topic) {
		case "all":
			hm_sport.setMap(hm_sport.getMap() ? null : map);
			hm_music.setMap(hm_music.getMap() ? null : map);
			hm_tech.setMap(hm_tech.getMap() ? null : map);
			hm_magic.setMap(hm_magic.getMap() ? null : map);
			break;
		case "sport":
			hm_sport.setMap(hm_sport.getMap() ? null : map);
			break;
		case "music":
			hm_music.setMap(hm_music.getMap() ? null : map);
			break;
		case "tech":
			hm_tech.setMap(hm_tech.getMap() ? null : map);
			break;
		case "magic":
			hm_magic.setMap(hm_magic.getMap() ? null : map);
			break;
		}
	}

	function changeGradient() {
		var gradient = [ 'rgba(0, 255, 255, 0)', 'rgba(0, 255, 255, 1)',
				'rgba(0, 191, 255, 1)', 'rgba(0, 127, 255, 1)',
				'rgba(0, 63, 255, 1)', 'rgba(0, 0, 255, 1)',
				'rgba(0, 0, 223, 1)', 'rgba(0, 0, 191, 1)',
				'rgba(0, 0, 159, 1)', 'rgba(0, 0, 127, 1)',
				'rgba(63, 0, 91, 1)', 'rgba(127, 0, 63, 1)',
				'rgba(191, 0, 31, 1)', 'rgba(255, 0, 0, 1)' ]
		hm_sport.set('gradient', hm_sport.get('gradient') ? null : gradient);
		hm_music.set('gradient', hm_music.get('gradient') ? null : gradient);
		hm_tech.set('gradient', hm_tech.get('gradient') ? null : gradient);
		hm_magic.set('gradient', hm_magic.get('gradient') ? null : gradient);
	}

	function changeRadius() {
		hm_sport.set('radius', hm_sport.get('radius') ? null : 20);
		hm_music.set('radius', hm_music.get('radius') ? null : 20);
		hm_tech.set('radius', hm_tech.get('radius') ? null : 20);
		hm_magic.set('radius', hm_magic.get('radius') ? null : 20);
	}

	function changeOpacity() {
		hm_sport.set('opacity', hm_sport.get('opacity') ? null : 0.2);
		hm_music.set('opacity', hm_music.get('opacity') ? null : 0.2);
		hm_tech.set('opacity', hm_tech.get('opacity') ? null : 0.2);
		hm_magic.set('opacity', hm_magic.get('opacity') ? null : 0.2);
	}

	/* Functions for websocket */
	function onOpen(evt) {
	}

	function onMessage(evt) {
		var tweet = JSON.parse(evt.data);
		switch (tweet.keyword) {
		case "sport":
			pa_sport.push(new google.maps.LatLng(parseFloat(tweet.latitude),
					parseFloat(tweet.longitude)));
			break;
		case "music":
			pa_music.push(new google.maps.LatLng(parseFloat(tweet.latitude),
					parseFloat(tweet.longitude)));
			break;
		case "tech":
			pa_tech.push(new google.maps.LatLng(parseFloat(tweet.latitude),
					parseFloat(tweet.longitude)));
			break;
		case "magic":
			pa_magic.push(new google.maps.LatLng(parseFloat(tweet.latitude),
					parseFloat(tweet.longitude)));
			break;
		}
		//websocket.close();
	}

	function onError(evt) {
		//writeToScreen('ERROR: ' + evt.data);
	}

	/* Output function for testing */
	function writeToScreen(message) {
		var pre = document.createElement("p");
		pre.style.wordWrap = "break-word";
		pre.innerHTML = message;
		output.appendChild(pre);
	}

	/* Request for new tweets per updateInt milliseconds */
	function newRequest(updateInt) {
		websocket.send(updateInt);
		removeOldData();
	}
	
	function removeOldData() {
		
	}

	/* Topic selection */
	function changeTopic() {
		var select = document.getElementById("topicList");
		var topic = select.options[select.selectedIndex].value;
		switch (topic) {
		case "all":
			hm_sport.setMap(map);
			hm_music.setMap(map);
			hm_tech.setMap(map);
			hm_magic.setMap(map);
			break;
		case "sport":
			hm_sport.setMap(map);
			hm_music.setMap(null);
			hm_tech.setMap(null);
			hm_magic.setMap(null);
			break;
		case "music":
			hm_sport.setMap(null);
			hm_music.setMap(map);
			hm_tech.setMap(null);
			hm_magic.setMap(null);
			break;
		case "tech":
			hm_sport.setMap(null);
			hm_music.setMap(null);
			hm_tech.setMap(map);
			hm_magic.setMap(null);
			break;
		case "magic":
			hm_sport.setMap(null);
			hm_music.setMap(null);
			hm_tech.setMap(null);
			hm_magic.setMap(map);
			break;
		}
		//websocket.send(topic);
	}

	/* Create the map */
	google.maps.event.addDomListener(window, 'load', initialize);

	var updateInt = 2000;
	window.setInterval(function() {
		newRequest(updateInt)
	}, updateInt);
</script>

</head>
<body>
	<div id="panel">
		<select id="topicList" onchange="changeTopic()" name="topicList">
			<option value="all" selected="selected">Choose a Topic</option>
			<option value="tech">Technology</option>
			<option value="sport">Sport</option>
			<option value="music">Music</option>
			<option value="magic">Magic</option>
		</select>
		<button onclick="toggleHeatmap()">Toggle Heatmap</button>
		<button onclick="changeGradient()">Change gradient</button>
		<button onclick="changeRadius()">Change radius</button>
		<button onclick="changeOpacity()">Change opacity</button>
		<br>
		<div id="output"></div>
	</div>
	<div id="map-canvas"></div>
</body>
</html>
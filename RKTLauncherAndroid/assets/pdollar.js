//
// Load PDollar for web
//
function loadPDollarWeb(){
	//blobal gesture_kit.
	gesture_kit = this;
	try{
		points = new Array(); // point array for current stroke
		_strokeID = 0;
		_r = new PDollarRecognizer();
	}catch(err){
		alert(err);
	}	
}

function loadGesturesWeb(data, callback){	
	//Load Gestures
	try {
	_r.PointClouds = new Array();	
	for (var i= 0; i < data.length; i++) {			
		var name = data[i].method;		
		// Check if web, then metadata here. 
		if (gk!="undefined"){
			var meta = data[i].metadata;	
			//alert("metadata: " + metadata + " metadata[name] : " + metadata[name]);
			if ( meta != "" && meta != null && _r.metadata[name] == undefined ){
				_r.metadata[name] = meta;
			} 	
		}		
		var pointaArray = new Array();				
		var gesture = data[i].gesture;				
		for (var j = 0; j < gesture.length; j++) {
			var id = gesture[j].ID;
			var X = gesture[j].X;
			var Y = gesture[j].Y;									
			pointaArray[j]  = new Point(X,Y,id);
		}				
		_r.PointClouds[i] = new PointCloud(name, pointaArray);			
	}

	}catch(err){
		alert(err);
		callback(false);
	}	
	
	callback(true);
	
	//alert(_r.PointClouds.toString());
}

//
// PDollar for All Addons
//

//
// Point class
//
function Point(x, y, id) // constructor
{
	this.X = x;
	this.Y = y;
	this.ID = id; // stroke ID to which this point belongs (1,2,...)
}

//
// PointCloud class: a point-cloud template
//
function PointCloud(name, points) // constructor
{
	this.Name = name;
	
	var newpoints = new Array(points[0]);
	for (var i = 1; i < points.length; i++)
		newpoints[i] = new Point(points[i].X, points[i].Y, points[i].ID);
	this.Points = newpoints;

	this.DrawingPoints = this.Points;
}
//
// Result class
//
function Result(name, score) // constructor
{
	this.Name = name;
	this.Score = score;
}
//
// PDollarRecognizer class constants
//
var NumPointClouds; // = 8;
var NumPoints = 32;
var Origin = new Point(0,0,0);
var RECOGNITION_THRESHOLD = 1.5;
var NO_MATCH_NAME = "No match.";
var NO_MATCH_SCORE = 0.0;

function setNumPointClouds(num){
	NumPointClouds = NumPointClouds + num;
} 
//
// PDollarRecognizer class
//
function PDollarRecognizer() // constructor
{
	//
	// one predefined point-cloud for each gesture
	//
	this.PointClouds = new Array(NumPointClouds);	
	ScopeClouds = this;	
	var PointCloudScope = this;
	this.metadata = {};
	//
	// The $P Point-Cloud Recognizer API begins here -- 3 methods: Recognize(), AddGesture(), DeleteUserGestures()
	//
	this.Recognize = function (points) {
		console.log("calling recognition");
		
	    points = Resample(points, NumPoints);
	    points = Scale(points);
	    points = TranslateTo(points, Origin);

	    var b1 = +Infinity;
	    var u1 = -1;
	    var b2 = +Infinity;
	    var u2 = -1;

		console.log("processing done");
		console.log(points.length);
		console.log("Clouds: " + this.PointClouds.length);
	    for (var i = 0; i < this.PointClouds.length; i++) // for each point-cloud template
	    {
			console.log("Step " + i + " " + this.PointClouds[i].Name);
	        var d = GreedyCloudMatch(points, this.PointClouds[i]);
	        if (d < b1) {
				if(u1 == -1 || this.PointClouds[i].Name != this.PointClouds[u1].Name)
	            {
					b2 = b1;
					u2 = u1;
				}
				
	            b1 = d; // best (least) distance
	            u1 = i; // point-cloud
	        }
	        else
	            if (d < b2 && this.PointClouds[i].Name != this.PointClouds[u1].Name) 
				{
	                b2 = d;
	                u2 = i;
	            }
			console.log("Step " + i + " finished");
	    }
		
		console.log("finished matching");
			  
	    if (u1 == -1)
	        return new Result(NO_MATCH_NAME, NO_MATCH_SCORE);
	    else {
			var d1 = GestureDistance(points, this.PointClouds[u1].Points);
	        var d2 = GestureDistance(points, this.PointClouds[u2].Points);
	        var name = "No match.";
			var best = 0.0;
			if (d2 < d1)
			{
				name = this.PointClouds[u2].Name;
				best = b2;
			}
			else
			{
				name = this.PointClouds[u1].Name;
				best = b1;
			}
			if(best<RECOGNITION_THRESHOLD)
				return new Result(name, Math.max((best - 2.0) / -2.0, 0.0));
			else
				return new Result(NO_MATCH_NAME, NO_MATCH_SCORE);
	    }
	};
	this.AddGesture = function(name, points)
	{
		this.PointClouds[this.PointClouds.length] = new PointCloud(name, points);
		var num = 0;
		for (var i = 0; i < this.PointClouds.length; i++) {
			if (this.PointClouds[i].Name == name)
				num++;
		}
		return num;
	};
	
	this.DeleteUserGestures = function()
	{
		this.PointClouds.length = NumPointClouds; // clear any beyond the original set
		return NumPointClouds;
	};
	
	this.LoadGestureSet = function(gestureset)
	{		
		PointCloudScope.PointClouds = new Array(gestureset.length);				
		for (var i= 0; i < gestureset.length; i++) {		
			var method = gestureset[i].method;				
			var pointaArray = new Array();				
			var gesture = gestureset[i].gesture;			
			for (var j = 0; j < gesture.length; j++) {
				var id = gesture[j].ID;
				var X = gesture[j].X;
				var Y = gesture[j].Y;									
				pointaArray[j]  = new Point(X,Y,id);
			}			
			PointCloudScope.PointClouds[i] = new PointCloud(method, pointaArray);			
		}
		console.log(PointCloudScope.toString);		
	};
	
}
//
// Private helper functions from this point down
//
function GreedyCloudMatch(points, P)
{
	console.log("gcm 1");
	var e = 0.50;
	var step = Math.floor(Math.pow(points.length, 1 - e));
	var min = +Infinity;
	console.log("gcm 2");
	for (var i = 0; i < points.length; i += step) {
		var d1 = CloudDistance(points, P.Points, i);
		var d2 = CloudDistance(P.Points, points, i);
		min = Math.min(min, Math.min(d1, d2)); // min3
	}
	console.log("gcm 4");
	return min;
}
function CloudDistance(pts1, pts2, start)
{	
	var matched = new Array(pts1.length); // pts1.length == pts2.length
	for (var k = 0; k < pts1.length; k++)
		matched[k] = false;
	var sum = 0;
	var i = start;
	do
	{
		var index = -1;
		var min = +Infinity;
		for (var j = 0; j < matched.length; j++)
		{
			if (!matched[j]) {
				var d = Distance(pts1[i], pts2[j]);
				if (d < min) {
					min = d;
					index = j;
				}
			}
		}
		matched[index] = true;
		var weight = 1 - ((i - start + pts1.length) % pts1.length) / pts1.length;
		sum += weight * min;
		i = (i + 1) % pts1.length;
	} while (i != start);
	return sum;
}
function Resample(points, n)
{
	var I = PathLength(points) / (n - 1); // interval length
	var D = 0.0;
	var newpoints = new Array(points[0]);
	for (var i = 1; i < points.length; i++)
	{
		if (points[i].ID == points[i-1].ID)
		{
			var d = Distance(points[i - 1], points[i]);
			if ((D + d) >= I)
			{
				var qx = points[i - 1].X + ((I - D) / d) * (points[i].X - points[i - 1].X);
				var qy = points[i - 1].Y + ((I - D) / d) * (points[i].Y - points[i - 1].Y);
				var q = new Point(qx, qy, points[i].ID);
				newpoints[newpoints.length] = q; // append new point 'q'
				points.splice(i, 0, q); // insert 'q' at position i in points s.t. 'q' will be the next i
				D = 0.0;
			}
			else D += d;
		}
	}
	if (newpoints.length == n - 1) // sometimes we fall a rounding-error short of adding the last point, so add it if so
		newpoints[newpoints.length] = new Point(points[points.length - 1].X, points[points.length - 1].Y, points[points.length - 1].ID);
	return newpoints;
}
function Scale(points)
{
	var minX = +Infinity, maxX = -Infinity, minY = +Infinity, maxY = -Infinity;
	for (var i = 0; i < points.length; i++) {
		minX = Math.min(minX, points[i].X);
		minY = Math.min(minY, points[i].Y);
		maxX = Math.max(maxX, points[i].X);
		maxY = Math.max(maxY, points[i].Y);
	}
	var size = Math.max(maxX - minX, maxY - minY);
	var newpoints = new Array();
	for (var i = 0; i < points.length; i++) {
		var qx = (points[i].X - minX) / size;
		var qy = (points[i].Y - minY) / size;
		newpoints[newpoints.length] = new Point(qx, qy, points[i].ID);
	}
	return newpoints;
}
function TranslateTo(points, pt) // translates points' centroid
{
	var c = Centroid(points);
	var newpoints = new Array();
	for (var i = 0; i < points.length; i++) {
		var qx = points[i].X + pt.X - c.X;
		var qy = points[i].Y + pt.Y - c.Y;
		newpoints[newpoints.length] = new Point(qx, qy, points[i].ID);
	}
	return newpoints;
}
function Centroid(points)
{
	var x = 0.0, y = 0.0;
	for (var i = 0; i < points.length; i++) {
		x += points[i].X;
		y += points[i].Y;
	}
	x /= points.length;
	y /= points.length;
	return new Point(x, y, 0);
}
function PathDistance(pts1, pts2) // average distance between corresponding points in two paths
{
	var d = 0.0;
	for (var i = 0; i < pts1.length; i++) // assumes pts1.length == pts2.length
		d += Distance(pts1[i], pts2[i]);
	return d / pts1.length;
}
function PathLength(points) // length traversed by a point path
{
	var d = 0.0;
	for (var i = 1; i < points.length; i++)
	{
		if (points[i].ID == points[i-1].ID)
			d += Distance(points[i - 1], points[i]);
	}
	return d;
}
function Distance(p1, p2) // Euclidean distance between two points
{	
	if (p2==undefined){
	 alert();
	}
	var dx = p2.X - p1.X;
	var dy = p2.Y - p1.Y;
	return Math.sqrt(dx * dx + dy * dy);	
}

function GestureDistance(g1, g2) {

    var d = 0.0;
	var nr = g1.length;
	if(g2.length < nr)
		nr = g2.length;

    for (var i = 0; i < nr; i++) {
        d = d + Distance(g1[i], g2[i]);
    }
	
	return d;
}

function Choose(best, secondBest, gesture) {
    var d1 = GestureDistance(gesture, best.Points);
    var d2 = GestureDistance(gesture, secondBest.Points);
    if (d2 < d1)
        return secondBest;
    else
        return best;
}


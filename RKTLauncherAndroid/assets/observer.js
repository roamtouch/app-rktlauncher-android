// Startup
var _isDown = 0, _points, _strokeID, _r, _g, _rc; // global variables
var _threshold = 500;

var pBridge;

$(document).ready(loadObserver());

function loadObserver(){
    try {
		console.log("loading observer");
        _points = new Array(); // point array for current stroke
        _strokeID = 0;
		_isDown = 0;
		_r = new PDollarRecognizer();
    } catch(err){
		console.log("loadObserver err: " + err);
        pBridge.outPut ( "err: " + err ); 
        //alert(err);
    }
}

// TouchDown 
function startPoint( x, y, _strokeID ){
    console.log("-------------------------start" + _strokeID);
	try {        
        // starting a new gesture
        if (_strokeID == 0){
            _points = new Array();
            _points.length = 0;
        }		
	    _points[_points.length] = new Point(x, y, _strokeID);
        _canRecognize = false;		
		_isDown  = _isDown + 1;		
    } catch(err){
		pBridge.outPut ( "error startPoint: " + err ); 
        //alert(err);
    }
	
	pBridge.outPut ( "startPoint canRecognize: " + _canRecognize ); 
}
// TouchMove 
function movePoint( x, y, _strokeID ){
    console.log("-------------------------move" + x + " " + y + " " +_strokeID);
	try {
			_points[_points.length] = new Point(x, y, _strokeID); // append
	}
    catch(err){
		pBridge.outPut ( "error movePoint: " + err );  
        //alert(err);
    }
}
// TouchEnd 
function endPoint(){
    console.log("-------------------------up");
	try 
	{
		
       	var target, pEvent;
		
		pBridge.outPut ( "endPoint : _isDown " + _isDown );  
		
		if (_isDown > 0)
		{
			recognizeTimer = setTimeout( 'recognizedGesture()', _threshold);	
			_isDown = _isDown - 1;
		}
		
		pBridge.outPut ( "_isDown::::::: " + _isDown );  		
		
		if(_isDown <= 1)	
		{
			_isDown = 0;
			_canRecognize = true;
		}
		
		//pBridge.outPut ( "endPoint canRecognize: " + _canRecognize ); 
		
	} catch (err) { 
		pBridge.outPut ( "error endPoint: " + err );  
		//alert(err); 
	}
}

function ProcessPoints(Points)
{
	var tempPoints = new Array();
	
	var max = 0;
	for(var i=0;i<Points.length;i++)
		if(Points[i].ID > max)
			max = Points[i].ID;
		
	for(var stroke=0; stroke<=max; stroke++)
	{
		for(var i=0;i<Points.length;i++)
			if(Points[i].ID == stroke)
				tempPoints[tempPoints.length] = new Point(Points[i].X, Points[i].Y, stroke);
	}
	return tempPoints;
}

function recognizedGesture(){

	pBridge.outPut ( "recognizedGesture canRecognize: "  + _canRecognize + "_points.length: "  + _points.length); 
	
    if (_canRecognize == true) {
        if (_points.length >= 5) {
		
			try {
				console.log(_points.length);
				var tempPoints = ProcessPoints(_points, _strokeID);
				console.log(tempPoints.length);
				console.log("calling recognition");
				var result = _r.Recognize(tempPoints);
            }
            catch (err) {
			    console.log("recognizedGesture: " + err)
            }          
			finally
			{
				clearGesture();
			}	
			if(result.Score != 0.0)
				pBridge.recognize ( result.Name, result.Score ); 	         
			else
				pBridge.recognize ( "CLEAR_VISOR", "0.6" ); 	         
        }
    }
}

function clearGesture(){
    clearTimeout(recognizeTimer);
	_points.length = 0;
	_strokeID = 0; // signal to begin new gesture on next mouse-down
	resultName = "";
	_isDown = 0;
}


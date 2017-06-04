<%-- 
    Document   : charting
    Created on : May 1, 2017, 8:26:46 PM
    Author     : PRABHAT RANJAN
--%>

<html ng-app="AngularChartExample">
<head>
    <title>Angularchart Example</title>
</head>
<body>
    <div ng-view></div>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.0.3/angular.js"></script>
    <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript"></script>
    <script src="http://code.highcharts.com/highcharts.js"></script>
    <script src="chart.js"></script>
    <script>
        function MainCtrl($scope, $http){
            var data = {"xData": ["Jan", "Feb", "Mar", "Apr", "May", "Jun","Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],"yData":[{
                "name": "Tokyo",
                "data": [<% out.write("9.0"); 
            
                
                %>, 6.9, 9.5, 14.5, 18.2, 21.5, 25.2, 26.5, 23.3, 18.3, 13.9, 9.6]
            } 
            ]}
            
            $scope.lineChartYData=data.yData
            $scope.lineChartXData=data.xData
        }
    </script>
    <script>
    angular.module('AngularChartExample',['AngularChart'], function( $routeProvider, $locationProvider ){
        $routeProvider.when('/',{
            template: '<chart title="Line chart example" xData="lineChartXData" yData="lineChartYData" xName="Month" yName="Hit" subtitle="This is an example"></chart>',
            controller: MainCtrl
            })
    })
    </script>
</body>
</html>
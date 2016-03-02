(function() {
    var app = angular.module('PersonalitiesApp', []);
    app.controller('PersonalitiesDemoController', ['$scope', function($scope) {

        var d3CirclePack = null;
        $scope.init = function(urlMap) {
            d3CirclePack = new InfopExpo.D3CirclePack();
            $scope.ws = new WebSocket(urlMap.wsUrl);
            $scope.ws.onmessage = function(event) {
              if (window.console) window.console.debug(event.data);
              var message = JSON.parse(event.data);
              switch(message.messageType) {
                  case "ClusterStart":
                      d3CirclePack.fadeOut();
                      $scope.$apply(function() {
                          $scope.heartBeats = 0;
                      });
                      break;
                  case "ClusterSuccess":
                      $scope.$apply(function() {
                          $scope.heartBeats = 0;
                      });
                      d3CirclePack.draw(urlMap.clusterPath.replace(":clusterId", message.data.clusterId));
                      break;
                  case "ClusterFail":
                      var errMsg = "Error in cluster computation: " + message.data.exception;
                      if (window.console) {
                          window.console.error(errMsg);
                      } else {
                          window.alert(errMsg);
                      }
                      break;
                  case "HeartBeat":
                      $scope.$apply(function() {
                          $scope.heartBeats += 1;
                      });
                      break;
                  default:
                      break;
              }
           };
        };

        $scope.persons = [
            {
                key: "napoleon",
                name: "Napoleon Bonaparte",
                sourcesCount: 4
            },
            {
                key: "churchill",
                name: "Sir Winston Churchill",
                sourcesCount: 3
            },
            {
                key: "gandhi",
                name: "Mahatma Gandhi",
                sourcesCount: 4
            }
        ];

        $scope.thumbnail = function(key, image_map) {
            return image_map[key];
        };

        $scope.heartBeats = 0;

        $scope.doAnalysis = function() {
            $scope.ws.send(JSON.stringify({k: -1, maxIter: -1}));
        };

    }]);
}());
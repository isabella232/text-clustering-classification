(function() {
    var app = angular.module('bbcDemoApp', []);
    app.controller('BBCDemoController', ['$scope', '$location', function($scope, $location) {

        $scope.heartBeats = 0;
        $scope.documents = {
            analyzed: 0
        };

        var d3CirclePack = null;
        $scope.init = function(opts) {

            d3CirclePack = new InfopExpo.D3CirclePack();

            $scope.ws = new WebSocket(opts.wsUrl);
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
                            $scope.documents.analyzed = message.data.processDocuments;
                            $scope.heartBeats = 0;
                            $scope.days.forEach(function(e) {
                                if (e.value <= $scope.dayHandlingOn) e.state = "on";
                            });
                            $scope.dayHandlingOn = false;
                        });
                        d3CirclePack.draw(opts.clusterPath.replace(":clusterId", message.data.clusterId));
                        break;
                    case "ClusterFail":
                        $scope.$apply(function() {
                            $scope.heartBeats = 0;
                            $scope.dayHandlingOn = false;
                        });
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

        var totalDays = 10;
        $scope.days = [];
        for (i = 0; i < totalDays; i++) {
            $scope.days.push({
                value: i + 1,
                state: "off"
            });
        }

        $scope.categories = [
            { value: "#tech",           title: "   Technology" },
            { value: "#business",       title: "     Business" },
            { value: "#sport",          title: "        Sport" },
            { value: "#politics",       title: "     Politics" },
            { value: "#entertainment",  title: "Entertainment" },
        ];

        $scope.selectedCategory = $scope.categories[0];

        $scope.otherCategoriesFn = function() {
            return $scope.categories.filter(function(e) {
                return e.value != $scope.selectedCategory.value;
            });
        };
        $scope.otherCategories = $scope.otherCategoriesFn();

        $scope.handleCategorySelection = function(event, catValue) {
            event.preventDefault();
            var newSelection = $scope.categories.find(function(e) {
                return e.value == catValue;
            });
            if (newSelection) {
                $scope.selectedCategory = newSelection;
                $scope.otherCategories = $scope.otherCategoriesFn();
                $scope.days.forEach(function(e) { e.state = "off"; });
                d3CirclePack.clear();
                $scope.documents.analyzed = 0;
            }
        };

        $scope.handleDay = function($event, dayValue) {
            $scope.dayHandlingOn = dayValue;
            $scope.days.forEach(function(e) {
                if (e.value <= dayValue) e.state = "na";
            });

            var cmd = {
                category: $scope.selectedCategory.value.substring(1),
                dayCount: dayValue,
                totalDays: totalDays
            }
            $scope.ws.send(JSON.stringify(cmd));
        };

    }]);
}());

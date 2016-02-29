var app = angular.module('bbcDemoApp', []);
app.controller('BBCDemoController', ['$scope', '$location', function($scope, $location) {

    var d3CirclePack = null;
    $scope.init = function(opts) {

        d3CirclePack = new InfopExpo.D3CirclePack();

        $scope.ws = new WebSocket(opts.wsUrl);
        $scope.ws.onmessage = function(event) {
            if (window.console) window.console.log(event.data);
            var id = event.data;
            d3CirclePack.fadeOut();
            d3CirclePack.draw(opts.clusterPath.replace(":clusterId", id));
        };
    };

    $scope.days = [];
    for (i = 0; i < 10; i++) {
        $scope.days.push({
            value: i + 1,
            on: false
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
            $scope.days.forEach(function(e) { e.on = false; });
            d3CirclePack.fadeOut();
        }
    };

    $scope.handleDay = function($event, dayValue) {
        $scope.days.forEach(function(e) {
            e.on = e.value <= dayValue ? true : false;
        });
        $scope.ws.send(dayValue);
    };

}]);

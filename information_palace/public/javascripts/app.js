var subscriptionApp = angular.module("SubscriptionApp", ["ngResource"]);

// Subscriptions

subscriptionApp
    .factory("Subscriptions", ["$resource", function($resource) {
      return $resource("/subscriptions/:id", {id: "@id"});
    }]);

subscriptionApp
    .controller("SubscriptionController", ["$scope", "Subscriptions", function($scope, Subscriptions) {

      var fetchAll = function() {
          Subscriptions.query()
              .$promise.then(function (feeds) {
              $scope.feeds = feeds;
          });
      };

      fetchAll();

      $scope.addFeed = function(feed) {
          Subscriptions.save(feed)
              .$promise.then(fetchAll);
      };

      $scope.feedURLInputKeyPressed = function(eventObject) {
        if (eventObject.which == 13) {
          var inputElement = $(eventObject.target);
          var feedUrl = inputElement.val();
          $scope.addFeed({url: feedUrl});
          inputElement.val("");
        }
      };

    }]);

// Analysis

subscriptionApp
    .factory("AnalysisRuns", ["$resource", function($resource) {
        return $resource("/analysis_runs/:id", {id: "@id"});
    }]);

subscriptionApp
    .controller("AnalysisController", ["$scope", "AnalysisRuns", function($scope, AnalysisRuns) {
        $scope.create = function(element) {
            $(element).attr("disabled", true);
            AnalysisRuns.save()
                .$promise
                .then(function() {
                    $(element).attr("disabled", false);
                });
        }
    }]);
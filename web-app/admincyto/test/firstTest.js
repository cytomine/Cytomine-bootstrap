describe("First Test", function () {
	// Arrange (set up a scenario)
	var counter;
	beforeEach(function () {
		counter = 0;
	});

	it("increments value", function () {
		// Act (attempt the operation)
		counter++;
		// Assert (verify the result)
		expect(counter).toEqual(1);
	})

	it("decrements value", function () {
		// Act (attempt the operation)
		counter--;
		// Assert (verify the result)
		expect(counter).toEqual(-1);
	})
});


describe("Controller Test", function () {
// Arrange
    var mockScope = {};
    var controller;

    beforeEach(angular.mock.module("exampleApp"));
    beforeEach(angular.mock.inject(function ($controller, $rootScope) {
        mockScope = $rootScope.$new();
        controller = $controller("defaultCtrl", {
            $scope: mockScope
        });
    }));
// Act and Assess
    it("Creates variable", function () {
        expect(mockScope.counter).toEqual(0);
    })
    it("Increments counter", function () {
        mockScope.incrementCounter();
        expect(mockScope.counter).toEqual(1);
    });
});

describe("User Test", function () {
    // Arrange
    var mockScope = {};
    var controller;

    beforeEach(angular.mock.module("cytomineUserArea"));
    beforeEach(angular.mock.inject(function ($controller, $rootScope) {
        mockScope = $rootScope.$new();
        controller = $controller("userCtrl", {
            $scope: mockScope
        });
    }));
    console.log(mockScope);
    it("get errors validation", function () {
        var msg = mockScope.getError({required:true});
        expect(msg).toEqual("Please enter a value");
    });
});


describe("Controller Test", function () {
// Arrange
    var mockScope, controller, backend;
    beforeEach(angular.mock.module("cytomineUserArea"));
    beforeEach(angular.mock.inject(function ($httpBackend) {
        backend = $httpBackend;
        backend.expect("GET", "http://localhost:8090/api/user.json").respond(
            {"collection":[ {"id": 100001,"created": "1399360892193","updated": null,"deleted": null,"username": "admin", "firstname": "Admin","lastname": "Master", "email": "lrollus@ulg.ac.be"},
                            {"id": 101,"created": "1399360891655","updated": "1399360891723","deleted": null,"username": "lrollus","firstname": "Loïc","lastname": "Rollus","email": "lrollus@ulg.ac.be"}],
                "offset": 0,
                "perPage": 2,
                "size": 2,
                "totalPages": 1.0});
    }));
    beforeEach(angular.mock.inject(function ($controller, $rootScope, $http) {
        mockScope = $rootScope.$new();
        $controller("userCtrl", {
            $scope: mockScope,
            $http: $http
        });
        //make the request now, trigger success function
        backend.flush();
    }));

    it("Makes an Ajax request", function () {
        //check if request is well done
        backend.verifyNoOutstandingExpectation();
    });
    it("Processes the data", function () {
        expect(mockScope.user.users).toBeDefined();
        expect(mockScope.user.users.length).toEqual(2);
    });
    it("Preserves the data order", function () {
        expect(mockScope.user.users[0].username).toEqual("admin");
        expect(mockScope.user.users[1].username).toEqual("lrollus");
    });
});

describe("Filter Tests", function () {
    var filterInstance;
    beforeEach(angular.mock.module("cytomineUserArea"));
    beforeEach(angular.mock.inject(function ($filter) {
        filterInstance = $filter("camelcase");
    }));
    it("Changes case", function () {
        expect(filterInstance("CYTOMINE")).toEqual("Cytomine");
        expect(filterInstance("a great app")).toEqual("A great app");
    });
});


describe("Service Tests", function () {
    beforeEach(angular.mock.module("cytomineUserArea"));
    it("Increments the counter", function () {
        angular.mock.inject(function (selectedUserService) {
            expect(selectedUserService.getSelectedUser()).toEqual({});
            selectedUserService.setSelectedUser({id:123,username:"lrollus"});
            expect(selectedUserService.getSelectedUser()).toBeDefined();
            expect(selectedUserService.getSelectedUserId()).toEqual(123);
        });
    });
});


//$scope.getAllUsers = function(callbackSuccess) {
//    userService.getAllUsers(
//        function(data) {
//            callbackSuccess(data);
//        },
//        function(data, status) {
//            $scope.user.error.retrieve = {status:status,message:data.errors};
//        }
//    );
//};
//
//$scope.loading = true;
//$scope.getAllUsers(
//    function(data) {
//        $scope.user.error.retrieve = null;
//        $scope.user.users = data;
//
//        $scope.tableParams = new ngTableParams({
//            page: 1,            // show first page
//            count: 10 ,          // count per page
//            sorting: {
//                username: 'asc'     // initial sorting
//            },
//            filter: {
//                username: ''       // initial filter
//            }
//        }, {
//            total: $scope.user.users.length, // length of data
//            getData: function($defer, params) {
//                // use build-in angular filter
//                var newData = $scope.user.users;
//                // use build-in angular filter
//                newData = params.filter() ?$filter('filter')(newData, params.filter()) : newData;
//                newData = params.sorting() ? $filter('orderBy')(newData, params.orderBy()) : newData;
//                $scope.data = newData.slice((params.page() - 1) * params.count(), params.page() * params.count())
//                params.total(newData.length); // set total for recalc pagination
//                $defer.resolve($scope.data);
//                $scope.loading = false;
//            }
//        });
//
//    });
//
//















//describe("Controller Test", function () {
//// Arrange
//    var mockScope, controller, backend;
//    beforeEach(angular.mock.module("exampleApp"));
//    beforeEach(angular.mock.inject(function ($httpBackend) {
//        backend = $httpBackend;
//        backend.expect("GET", "productData.json").respond(
//            [{ "name": "Apples", "category": "Fruit", "price": 1.20 },
//                { "name": "Bananas", "category": "Fruit", "price": 2.42 },
//                { "name": "Pears", "category": "Fruit", "price": 2.02 }]);
//    }));
//    beforeEach(angular.mock.inject(function ($controller, $rootScope, $http) {
//        mockScope = $rootScope.$new();
//        $controller("defaultCtrl", {
//            $scope: mockScope,
//            $http: $http
//        });
//        backend.flush();
//    }));
//// Act and Assess
//    it("Creates variable", function () {
//        expect(mockScope.counter).toEqual(0);
//    })
//    it("Increments counter", function () {
//        mockScope.incrementCounter();
//        expect(mockScope.counter).toEqual(1);
//    });
//    it("Makes an Ajax request", function () {
//        backend.verifyNoOutstandingExpectation();
//    });
//    it("Processes the data", function () {
//        expect(mockScope.products).toBeDefined();
//        expect(mockScope.products.length).toEqual(3);
//    });
//    it("Preserves the data order", function () {
//        expect(mockScope.products[0].name).toEqual("Apples");
//        expect(mockScope.products[1].name).toEqual("Bananas");
//        expect(mockScope.products[2].name).toEqual("Pears");
//    });
//});
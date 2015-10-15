describe 'TemplateService', ->

    service = {}
    $httpBackend = null

    beforeEach module 'webcompiler'

    beforeEach inject ($injector) ->
        $httpBackend = $injector.get '$httpBackend'
        service = $injector.get 'TemplateService'

    afterEach ->
        $httpBackend.verifyNoOutstandingExpectation()
        $httpBackend.verifyNoOutstandingRequest()

    describe '"get" function', ->

        beforeEach ->
            $httpBackend.expectGET 'templates/c_basic.c'
            $httpBackend.when('GET', 'templates/c_basic.c').respond data: 'c_basic_content'

        it 'uses $http to retrieve the template file', ->
            service.get 'c_basic.c', jasmine.createSpy 'callbackMock'
            $httpBackend.flush()

        it 'returns the correct part of http response', ->

            callback = jasmine.createSpy 'callbackMock'
            service.get 'c_basic.c', callback
            $httpBackend.flush()
            expect(callback).toHaveBeenCalled()

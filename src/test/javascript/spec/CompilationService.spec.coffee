describe 'CompilationService', ->

    # The service under test
    service = null

    # Mock for angular $http service
    $httpBackend = null

    # error and callback spies
    error = callback = null

    beforeEach -> module 'webcompiler'

    beforeEach ->
        inject ($injector) ->
            $httpBackend = $injector.get '$httpBackend'
            service = $injector.get 'CompilationService'

        callback = jasmine.createSpy 'callback'
        error = jasmine.createSpy 'error'

    afterEach ->
        $httpBackend.verifyNoOutstandingExpectation()
        $httpBackend.verifyNoOutstandingRequest()

    describe '"Compile" function', ->
        expectation = null
        beforeEach ->
            expectation = $httpBackend.expectPOST('/programs')

        describe 'in the nominal case', ->
            beforeEach ->
                expectation.respond
                    status: 'OK'
                    programId: '1'

            it 'should call the callback', ->
                service.compile 'testSource', callback, error
                $httpBackend.flush()
                expect(callback).toHaveBeenCalled()
                expect(error).not.toHaveBeenCalled()

        describe 'when the request fails', ->
            beforeEach ->
                expectation.respond 500, 'error'

            it 'should call the error callback', ->
                service.compile 'testSource', callback, error
                $httpBackend.flush()
                expect(callback).not.toHaveBeenCalled()
                expect(error).toHaveBeenCalled()

        describe 'when the API returns an error', ->
            beforeEach ->
                expectation.respond status: 'KO'

            it 'should call the error callback', ->
                service.compile 'testSource', callback, error
                $httpBackend.flush()
                expect(callback).not.toHaveBeenCalled()
                expect(error).toHaveBeenCalled()

    describe '"Execute" function', ->
        expectation = null
        beforeEach ->
            expectation = $httpBackend.expectPOST('/programs/undefined/executions')

        describe 'in the nominal case', ->
            beforeEach ->
                expectation.respond
                    status: 'OK'

            it 'should call the callback', ->
                service.execute callback, error
                $httpBackend.flush()
                expect(callback).toHaveBeenCalled()
                expect(error).not.toHaveBeenCalled()

        describe 'when the request fails', ->
            beforeEach ->
                expectation.respond 500, 'error'

            it 'should call the error callback', ->
                service.execute callback, error
                $httpBackend.flush()
                expect(callback).not.toHaveBeenCalled()
                expect(error).toHaveBeenCalled()

        describe 'when the API returns an error', ->
            beforeEach ->
                expectation.respond status: 'KO'

            it 'should call the error callback', ->
                service.execute callback, error
                $httpBackend.flush()
                expect(callback).not.toHaveBeenCalled()
                expect(error).toHaveBeenCalled()

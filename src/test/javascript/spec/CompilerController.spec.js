'use strict';

describe('CompilerController', function() {
  beforeEach(module('webcompiler'));

  var $controller;

  beforeEach(inject(function(_$controller_) {
    $controller = _$controller_;
  }));

  describe('$controller.loadTemplate', function() {
    it('has a test that always pass', function() {
      expect(true).toBe(true);
    });
  });
});

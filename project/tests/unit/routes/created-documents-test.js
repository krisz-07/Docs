import { module, test } from 'qunit';
import { setupTest } from 'project/tests/helpers';

module('Unit | Route | created-documents', function (hooks) {
  setupTest(hooks);

  test('it exists', function (assert) {
    let route = this.owner.lookup('route:created-documents');
    assert.ok(route);
  });
});

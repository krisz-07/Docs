import { module, test } from 'qunit';
import { setupTest } from 'project/tests/helpers';

module('Unit | Route | create-document', function (hooks) {
  setupTest(hooks);

  test('it exists', function (assert) {
    let route = this.owner.lookup('route:create-document');
    assert.ok(route);
  });
});

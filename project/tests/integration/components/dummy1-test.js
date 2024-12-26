import { module, test } from 'qunit';
import { setupRenderingTest } from 'project/tests/helpers';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | dummy1', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(hbs`<Dummy1 />`);

    assert.dom().hasText('');

    // Template block usage:
    await render(hbs`
      <Dummy1>
        template block text
      </Dummy1>
    `);

    assert.dom().hasText('template block text');
  });
});

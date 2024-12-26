import { module, test } from 'qunit';
import { setupRenderingTest } from 'project/tests/helpers';
import { render } from '@ember/test-helpers';
import { hbs } from 'ember-cli-htmlbars';

module('Integration | Component | created-document-list', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function (assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(hbs`<CreatedDocumentList />`);

    assert.dom().hasText('');

    // Template block usage:
    await render(hbs`
      <CreatedDocumentList>
        template block text
      </CreatedDocumentList>
    `);

    assert.dom().hasText('template block text');
  });
});

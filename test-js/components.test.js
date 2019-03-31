import React from 'react';
import renderer from 'react-test-renderer';

import { com } from "./components";

const {
    Widget
} = com.onionpancakes.veil.test_js.components;


it("Widget", () => {
    let component = renderer.create(
        <Widget></Widget>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});
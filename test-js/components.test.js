import React from 'react';
import renderer from 'react-test-renderer';

import "./components";

const {
    Widget,
    WidgetNested,
    WidgetKeyword,
    WidgetMapProps,
    WidgetFor,
    WidgetFragment
} = dev.onionpancakes.veil.test_js.components;


it("Widget", () => {
    let component = renderer.create(
        <Widget></Widget>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});

it("WidgetNested", () => {
    let component = renderer.create(
        <WidgetNested></WidgetNested>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});

it("WidgetKeyword", () => {
    let component = renderer.create(
        <WidgetKeyword></WidgetKeyword>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});

it("WidgetMapProps", () => {
    let component = renderer.create(
        <WidgetMapProps></WidgetMapProps>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});

it("WidgetFor", () => {
    let component = renderer.create(
        <WidgetFor></WidgetFor>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});

it("WidgetFragment", () => {
    let component = renderer.create(
        <div>
            <WidgetFragment></WidgetFragment>
        </div>
    );
    let tree = component.toJSON();
    expect(tree).toMatchSnapshot();
});
